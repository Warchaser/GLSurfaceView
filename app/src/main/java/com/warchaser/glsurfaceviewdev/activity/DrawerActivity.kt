package com.warchaser.glsurfaceviewdev.activity

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.base.FragmentStackBean
import com.warchaser.glsurfaceviewdev.base.FragmentStackManager
import com.warchaser.glsurfaceviewdev.fragment.drawer.*
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_drawer.*

/**
 * 抽屉activity
 * 包含NavigationView
 * */
class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var mLastMenuItem : MenuItem ? = null

    private var mTempFragment : Fragment ? = null

    private val HOME_FRAGMENT : String = "HOME_FRAGMENT"
    private val CATEGORY_FRAGMENT : String = "CATEGORY_FRAGMENT"
    private val DYNAMIC_FRAGMENT : String = "DYNAMIC_FRAGMENT"
    private val COMMUNICATE_FRAGMENT : String = "COMMUNICATE_FRAGMENT"

    private val VIP_FRAGMENT : String = "VIP_FRAGMENT"
    private val DOWNLOAD_FRAGMENT : String = "DOWNLOAD_FRAGMENT"

    private var mCurrentFragmentTag : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        initialize()
    }

    private fun initialize(){
        mNavigationView.setNavigationItemSelectedListener(this)

        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(COMMUNICATE_FRAGMENT, CommunicateFragment()))
        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(DYNAMIC_FRAGMENT, DynamicFragment()))
        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(CATEGORY_FRAGMENT, CategoryFragment()))
        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(HOME_FRAGMENT, HomeFragment()))

        mRadioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when(checkedId){
                    R.id.mFirstBtn->{
                        mCurrentFragmentTag = HOME_FRAGMENT
                    }
                    R.id.mSecondBtn->{
                        mCurrentFragmentTag = CATEGORY_FRAGMENT
                    }
                    R.id.mThirdBtn->{
                        mCurrentFragmentTag = DYNAMIC_FRAGMENT
                    }
                    R.id.mForthBtn->{
                        mCurrentFragmentTag = COMMUNICATE_FRAGMENT
                    }
                }
                switchFragment(FragmentStackManager.getInstance().getCertainFragment(mCurrentFragmentTag).fragment)
            }
        })

        mRadioGroup.check(R.id.mFirstBtn)
    }

    private fun jumpMenuFragment(tag : String, currentFragment: Fragment, className : String){
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        var target : Fragment? = supportFragmentManager.findFragmentByTag(tag)
        if(target == null){
            target = Class.forName(className).newInstance() as Fragment

            FragmentStackManager.getInstance().pushFragment(FragmentStackBean(tag, target))

            transaction.hide(currentFragment)
                    .add(getContainerId(), target, tag)
                    .addToBackStack(tag)
                    .commit()
        } else {
            transaction.hide(currentFragment).show(target).commit()
        }

        mTempFragment = currentFragment
    }

    private fun switchFragment(currentFragment: Fragment?){
        if(mTempFragment !== currentFragment){
            if(currentFragment != null){
                val ft = supportFragmentManager.beginTransaction()
                if(!currentFragment.isAdded){
                    if(mTempFragment != null){
                        ft.hide(mTempFragment!!)
                    }

                    ft.add(getContainerId(), currentFragment)
                } else {
                    if(mTempFragment != null){
                        ft.hide(mTempFragment!!)
                    }
                    ft.show(currentFragment)
                }
                ft.commit()
            }
        }
        mTempFragment = currentFragment
    }

    private fun changeFragmentIndex(tag : String, item : MenuItem, isHomeFragment: Boolean, className: String){

        mLastMenuItem?.run {
            isChecked = false
        }

        if(isHomeFragment){
            switchFragment(FragmentStackManager.getInstance().getCertainFragment(mCurrentFragmentTag).fragment)
        } else {
            jumpMenuFragment(tag, FragmentStackManager.getInstance().getCertainFragment(mCurrentFragmentTag).fragment, className)
        }

        mCurrentFragmentTag = tag
        item.isChecked = true
        mLastMenuItem = item
    }

    private fun hideBottomBar(){
        mRadioGroup.visibility = View.GONE
    }

    private fun showBottomBar(){
        mRadioGroup.visibility = View.VISIBLE
    }

    private fun getContainerId() : Int{
        return R.id.mContainer
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if(p0.itemId == R.id.item_home){
            showBottomBar()
        } else {
            hideBottomBar()
        }

        mDrawerLayout.closeDrawer(GravityCompat.START)
        when(p0.itemId){
            R.id.item_home->{
                changeFragmentIndex(HOME_FRAGMENT, p0, true, "")
                return true
            }
            R.id.item_vip->{
                changeFragmentIndex(VIP_FRAGMENT, p0, false, "com.warchaser.glsurfaceviewdev.fragment.drawer.MyBigMemberFragment")
                return true
            }
            R.id.item_download->{
                changeFragmentIndex(DOWNLOAD_FRAGMENT, p0, false, "com.warchaser.glsurfaceviewdev.fragment.drawer.DownloadFragment")
                return true
            }
            R.id.item_favourite->{
//                changeFragmentIndex(2 + LENGTH, p0)
                return true
            }
            R.id.item_history->{
//                changeFragmentIndex(3 + LENGTH, p0)
                return true
            }
            R.id.item_group->{
//                changeFragmentIndex(4 + LENGTH, p0)
                return true
            }
            R.id.item_tracker->{
//                changeFragmentIndex(5 + LENGTH, p0)
                return true
            }
            R.id.item_theme->{
//                changeFragmentIndex(6 + LENGTH, p0)
                return true
            }
            R.id.item_app->{
//                changeFragmentIndex(7 + LENGTH, p0)
                return true
            }
            R.id.item_settings->{
//                changeFragmentIndex(8 + LENGTH, p0)
                return true
            }
            else->{
                return false
            }
        }
    }

    override fun onBackPressed() {
        val amount = supportFragmentManager.backStackEntryCount
        if(amount > 0){
            FragmentStackManager.getInstance().popLastFragments(supportFragmentManager.backStackEntryCount)
            supportFragmentManager.popBackStackImmediate(null, 1)
            showBottomBar()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        clearFindViewByIdCache()
        super.onDestroy()
    }


}