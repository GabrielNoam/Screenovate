package com.screenovate.superdo

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.screenovate.superdo.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MainActivity
 * @author Gabriel Noam
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * MAX_ITEMS is the max count of groceries to
         * maintain in cache for retrieve
         */
        private const val MAX_ITEMS = 100
    }

    private lateinit var db: GroceriesDatabase
    private val data: MutableList<Grocery> = mutableListOf()
    private lateinit var viewModel: GroceriesViewModel
    private lateinit var recyclerViewAdapter: GroceriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initView()
        initCache()
    }

    private fun initObservers() {
        viewModel.filter.observe(this,
            Observer<String> {

                val value: Float = it.toFloatOrZero()

                val filter =
                    { grocery: Grocery ->
                        value == 0F || grocery.weightAsFloat() < value }

                viewModel.filter(filter)

                Snackbar.make(constraintLayout, "filter $it", Snackbar.LENGTH_SHORT).show()
            })

        viewModel.groceries.observe(this, observer)
    }

    /**
     * Loading last <code>MAX_ITEMS</code> groceries from cache
     */
    private fun initCache() {
        GlobalScope.launch(Dispatchers.IO) {
            // Init database and latest MAX_ITEMS groceries from cache
            db = GroceriesDatabase.getInstance(this@MainActivity)
            data.addAll(db.groceryDao().loadAll())
            data.reverse()

            withContext(Dispatchers.Main) {
                recyclerViewAdapter.notifyDataSetChanged()

                // When cache is loaded, start to observe
                initObservers()
            }
        }
    }

    /**
     * Init Layout and ViewModel binding
     */
    private fun initBinding() {
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(GroceriesViewModel::class.java)
        binding.viewModel = viewModel
        lifecycle.addObserver(viewModel)
    }

    /**
     * Views init
     */
    private fun initView() {
        setTitle(R.string.incoming_item_feed)

        recyclerViewAdapter = GroceriesAdapter(data)

        recyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutAnimation = AnimationUtils
                .loadLayoutAnimation(this@MainActivity,
                    R.anim.layout_animation_fall_down)
        }
    }

    private val observer =
            Observer<List<Grocery>> { groceries ->

                GlobalScope.launch(Dispatchers.IO) {
                    groceries?.takeUnless { it.isEmpty() }?.let {
                        handleMaxItems(it)
                        withContext(Dispatchers.Main) {
                            showStatus(it)
                            when(data.size) {
                                1 -> recyclerViewAdapter.notifyDataSetChanged()
                                else -> recyclerViewAdapter.notifyItemInserted(0)
                            }
                            recyclerView.smoothScrollToPosition(0)
                        }
                    }
                }
            }

    /**
     * Clean out cache when its size exceeds <code>MAX_ITEMS</code> size
     */
    private suspend fun handleMaxItems(groceries: List<Grocery>) {
        if(groceries.size > MAX_ITEMS) {
            data.clear()
            db.groceryDao().deleteAll()
        }

        data.add(0, groceries.last())
    }

    private fun showStatus(groceries: List<Grocery>) {
        if(groceries.size == MAX_ITEMS) {
            Snackbar.make(constraintLayout, "Exceeded $MAX_ITEMS items, clear list ...", Snackbar.LENGTH_SHORT).show()
        }
    }
}