package com.warchaser.glsurfaceviewdev.activity

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.warchaser.glsurfaceviewdev.R
import kotlinx.android.synthetic.main.activity_drawer.*

/**
 * 抽屉activity
 * 包含NavigationView
 * */
class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{


    private var mLastMenuItem : MenuItem ? = null

    /**
     * 底部选中图标Id数组
     * */
    private val mIconSelectedIds : IntArray = intArrayOf(
            R.mipmap.ic_home_selected,
            R.mipmap.ic_category_selected,
            R.mipmap.ic_dynamic_selected,
            R.mipmap.ic_communicate_selected
    )

    /**
     * 底部未选中图标Id数组
     * */
    private val mIconUnSelectedIds : IntArray = intArrayOf(
            R.mipmap.ic_home_unselected,
            R.mipmap.ic_category_unselected,
            R.mipmap.ic_dynamic_unselected,
            R.mipmap.ic_communicate_unselected
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        initialize()
    }

    private fun initialize(){
        mNavigationView.setNavigationItemSelectedListener(this)
    }

    private fun changeFragmentIndex(position : Int, item : MenuItem){
        mLastMenuItem?.run {
            isChecked = false
        }
        item.isChecked = true
        mLastMenuItem = item
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        mDrawerLayout.closeDrawer(GravityCompat.START)
        when(p0.itemId){
            R.id.item_home->{
                changeFragmentIndex(0, p0)
                return true
            }
            R.id.item_vip->{
                changeFragmentIndex(1, p0)
                return true
            }
            R.id.item_download->{
                changeFragmentIndex(2, p0)
                return true
            }
            R.id.item_favourite->{
                changeFragmentIndex(3, p0)
                return true
            }
            R.id.item_history->{
                changeFragmentIndex(4, p0)
                return true
            }
            R.id.item_group->{
                changeFragmentIndex(5, p0)
                return true
            }
            R.id.item_tracker->{
                changeFragmentIndex(6, p0)
                return true
            }
            R.id.item_theme->{
                changeFragmentIndex(7, p0)
                return true
            }
            R.id.item_app->{
                changeFragmentIndex(8, p0)
                return true
            }
            R.id.item_settings->{
                changeFragmentIndex(9, p0)
                return true
            }
            else->{
                return false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}