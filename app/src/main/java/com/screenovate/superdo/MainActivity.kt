package com.screenovate.superdo

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
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
        private const val TAG = "MainActivity"
        /**
         * MAX_ITEMS is the max count of groceries to
         * maintain in cache for retrieve
         */
        private const val MAX_ITEMS = 100
        private const val FIRST_INDEX = 0
    }

    private var item: MenuItem? = null
    private lateinit var db: GroceriesDatabase
    private lateinit var viewModel: GroceriesViewModel
    private lateinit var recyclerViewAdapter: GroceriesAdapter

    private val data: MutableList<Grocery> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initView()
        initCache()
    }

    private fun initObservers() {
        viewModel.message.observe(
            this,
            Observer<Msg> {
                when(it.type) {
                    Msg.Type.Snack ->
                        Snackbar.make(constraintLayout, it.message, Snackbar.LENGTH_SHORT).show()
                    Msg.Type.Dialog ->
                        AlertDialog.Builder(this).setTitle("Alert").setMessage(it.message)
                            .setPositiveButton("OK", null).create().show()
                    Msg.Type.Log ->
                        Log.d(TAG, it.message)
                }
            })

        viewModel.feedState.observe(
            this,
            Observer<FeedStatus> { feedState ->
                item?.also {
                    when(feedState!!) {
                        FeedStatus.ERROR -> {
                            it.setIcon(R.drawable.ic_menu_error)
                            item?.tooltipText = getString(R.string.error_feed)
                            showStatus(R.string.error_feed)
                        }
                        FeedStatus.OFF -> {
                            it.setIcon(R.drawable.ic_menu_start)
                            item?.tooltipText = getString(R.string.click_to_start_feed)
                            showStatus(R.string.stop_feed)
                        }
                        FeedStatus.ON -> {
                            it.setIcon(R.drawable.ic_menu_stop)
                            item?.tooltipText = getString(R.string.click_to_stop_feed)
                            showStatus(R.string.start_feed)
                        }
                    }
                }
            })

        viewModel.groceries.observe(this, Observer<List<Grocery>> { groceries ->

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
        })
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
                showStatus(R.string.cached_feed)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        initMenu(menu)

        return true
    }

    /**
     * onFeed is used in main_menu.xml do not change this method without refactor it
     */
    fun onFeed(item: MenuItem) {
        when(viewModel.feedState.value) {
            FeedStatus.ON -> viewModel.disconnect()
            FeedStatus.OFF -> viewModel.connect()
            FeedStatus.ERROR -> showStatus(getString(R.string.error_feed))
        }
    }

    private fun initMenu(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        item = menu.findItem(R.id.feed)
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            tooltipText = getString(R.string.click_to_filter_feed)
            queryHint = getString(R.string.filter_hint)
            setIconifiedByDefault(true)
            setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val value: Float = query.toFloatOrZero()

                    if(value > 0F)
                        showStatus(String.format(getString(R.string.filter_under), value))
                    else
                        @Suppress("DEPRECATION")
                        // would use onActionViewCollapsed() instead if it was working as should
                        MenuItemCompat.collapseActionView(menu.findItem(R.id.search))

                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    val value: Float = query.toFloatOrZero()
                    val filter =
                        { grocery: Grocery ->
                            value == 0F || grocery.weightAsFloat() < value }

                    viewModel.filter(filter)

                    val message =
                        if(value > 0)
                            String.format(getString(R.string.filter_under), value)
                        else
                            getString(R.string.no_filter)

                    showStatus(message)

                    return true
                }
            })
        }

        item?.apply {
            tooltipText = getString(R.string.click_to_stop_feed)
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

        data.add(FIRST_INDEX, groceries.last())
    }

    private fun showStatus(@StringRes id: Int) {
        showStatus(getString(id))
    }
    private fun showStatus(message: String) {
        Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showStatus(groceries: List<Grocery>) {
        if(groceries.size == MAX_ITEMS) {
            Snackbar.make(constraintLayout,
                String.format(getString(R.string.exceed_max_items), MAX_ITEMS),
                    Snackbar.LENGTH_SHORT).show()
        }
    }
}