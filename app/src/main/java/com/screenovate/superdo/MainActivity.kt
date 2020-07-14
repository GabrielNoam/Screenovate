package com.screenovate.superdo

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.databinding.DataBindingUtil
import com.screenovate.superdo.databinding.ActivityMainBinding

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
    private lateinit var data: MutableList<Grocery>
    private lateinit var viewModel: GroceriesViewModel
    private lateinit var recyclerViewAdapter: GroceriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        initFromCache()
    }

    /**
     * Loading last <code>MAX_ITEMS</code> groceries from cache
     */
    private fun initFromCache() {
        GlobalScope.launch(Dispatchers.IO) {
            // Init database and last data in cache
            db = GroceriesDatabase.getInstance(this@MainActivity)
            data = db.groceryDao().loadAll()
            data.reverse()
            withContext(Dispatchers.Main) {
                // Init views as data prepared
                initView()
            }
        }
    }

    /**
     * Init Layout and ViewModel binding
     */
    private fun initBinding() {
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this).get(GroceriesViewModel::class.java)
        binding.viewModel = viewModel
        lifecycle.addObserver(viewModel)

        viewModel.filter.observe(this,
            Observer<String> {
                // TODO Support Generic Filter with hi-order function
                val value = if(it.isNullOrEmpty()) 0F else it.toFloat()
                viewModel.filterFeed(value)
                Snackbar.make(constraintLayout, "filter $it", Snackbar.LENGTH_SHORT).show()
            })

        setTitle(R.string.incoming_item_feed)
    }

    private fun initView() {
        recyclerViewAdapter = GroceriesAdapter(data)

        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)

        recyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutAnimation = animation
        }

        viewModel = ViewModelProviders.of(this).get(GroceriesViewModel::class.java)
        viewModel.groceries.observe(this, observer)
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