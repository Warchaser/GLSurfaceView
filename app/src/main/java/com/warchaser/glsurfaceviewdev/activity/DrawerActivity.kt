package com.warchaser.glsurfaceviewdev.activity

import android.content.Intent
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RadioGroup
import com.google.android.material.internal.NavigationMenuView
import com.warchaser.glsurfaceviewdev.R
import com.warchaser.glsurfaceviewdev.app.BaseActivity
import com.warchaser.glsurfaceviewdev.base.FragmentStackBean
import com.warchaser.glsurfaceviewdev.base.FragmentStackManager
import com.warchaser.glsurfaceviewdev.fragment.drawer.*
import com.warchaser.titlebar.util.StatusBarUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_drawer.*

/**
 * 抽屉activity
 * 包含NavigationView
 * */
class DrawerActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var mLastMenuItem : MenuItem ? = null

    private var mTempFragment : Fragment? = null

    private val HOME_FRAGMENT : String = "HOME_FRAGMENT"
    private val CATEGORY_FRAGMENT : String = "CATEGORY_FRAGMENT"
    private val DYNAMIC_FRAGMENT : String = "DYNAMIC_FRAGMENT"
    private val COMMUNICATE_FRAGMENT : String = "COMMUNICATE_FRAGMENT"

    private val VIP_FRAGMENT : String = "com.warchaser.glsurfaceviewdev.fragment.drawer.MyBigMemberFragment"
    private val DOWNLOAD_FRAGMENT : String = "com.warchaser.glsurfaceviewdev.fragment.drawer.DownloadFragment"
    private val MY_COLLECTION_FRAGMENT : String = "com.warchaser.glsurfaceviewdev.fragment.drawer.CollectionFragment"
    private val HISTORY_FRAGMENT : String = "com.warchaser.glsurfaceviewdev.fragment.drawer.HistoryFragment"
    private val MY_FOLLOWING_FRAGMENT : String = "com.warchaser.glsurfaceviewdev.fragment.drawer.FollowingFragment"

    private var mCurrentFragmentTag : String = ""

    private var mClickedMenuItem : MenuItem ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        initialize()
    }

    private fun initialize(){
        mNavigationView.setNavigationItemSelectedListener(this)
        val menuView = mNavigationView.getChildAt(0)
        if(menuView != null && menuView is NavigationMenuView){
            menuView.isVerticalScrollBarEnabled  = false

            menuView.setPadding(0, 0 ,0 , mLyDrawerBottomMenu.layoutParams.height)
        }

        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(COMMUNICATE_FRAGMENT, CommunicateFragment()))
        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(DYNAMIC_FRAGMENT, DynamicFragment()))
        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(CATEGORY_FRAGMENT, CategoryFragment()))
        FragmentStackManager.getInstance().pushFragment(FragmentStackBean(HOME_FRAGMENT, HomeFragment()))

        mDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(p0: Int) {

            }

            override fun onDrawerSlide(p0: View, p1: Float) {

            }

            override fun onDrawerClosed(p0: View) {

                val itemId = getClickedMenuItem()?.itemId
                if(itemId == R.id.item_vip
                        || itemId == R.id.item_download
                        || itemId == R.id.item_favourite
                        || itemId == R.id.item_history
                        || itemId == R.id.item_group){
                    hideBottomBar()
                }

                when(itemId){
                    R.id.item_home->{
                        back2Home()
                    }
                    R.id.item_vip->{
                        changeFragmentIndex(VIP_FRAGMENT, getClickedMenuItem())
                    }
                    R.id.item_download->{
                        changeFragmentIndex(DOWNLOAD_FRAGMENT, getClickedMenuItem())
                    }
                    R.id.item_favourite->{
                        changeFragmentIndex(MY_COLLECTION_FRAGMENT, getClickedMenuItem())
                    }
                    R.id.item_history->{
                        changeFragmentIndex(HISTORY_FRAGMENT, getClickedMenuItem())
                    }
                    R.id.item_group->{
                        changeFragmentIndex(MY_FOLLOWING_FRAGMENT, getClickedMenuItem())
                    }
                    R.id.item_tracker->{
                        Intent(this@DrawerActivity, MyWalletActivity::class.java).apply {
                            startActivity(this)
                        }
                    }
                    R.id.item_theme->{
                        Intent(this@DrawerActivity, ThemeSelectionActivity::class.java).apply {
                            startActivity(this)
                        }
                    }
                    R.id.item_app->{
                        Intent(this@DrawerActivity, AppRecommendationActivity::class.java).apply {
                            startActivity(this)
                        }
                    }
                    R.id.item_settings->{
//                changeFragmentIndex(8 + LENGTH, p0)
                    }
                    else->{

                    }
                }
            }

            override fun onDrawerOpened(p0: View) {

            }

        })

        mRadioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                setCurrentTag(checkedId)
                switchFragment(FragmentStackManager.getInstance().getCertainFragment(mCurrentFragmentTag).fragment)
            }
        })

        mDrawerLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                StatusBarUtil.cutOutTitleBarCus(mTitleBar, this@DrawerActivity)
                mDrawerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        mRadioGroup.check(R.id.mFirstBtn)
    }

    private fun jumpMenuFragment(tag : String, currentFragment: Fragment){
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        var target : Fragment? = supportFragmentManager.findFragmentByTag(tag)
        if(target == null){
            target = Class.forName(tag).newInstance() as Fragment

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

    private fun changeFragmentIndex(tag : String, item : MenuItem?){

        mLastMenuItem?.run {
            isChecked = false
        }

        jumpMenuFragment(tag, FragmentStackManager.getInstance().getCertainFragment(mCurrentFragmentTag).fragment)

        mCurrentFragmentTag = tag
        item?.isChecked = true
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

    private fun setCurrentTag(checkedId : Int){
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
    }

    private fun back2Home(){
        FragmentStackManager.getInstance().popLastFragments(supportFragmentManager.backStackEntryCount)
        supportFragmentManager.popBackStackImmediate(null, 1)

        setCurrentTag(mRadioGroup.checkedRadioButtonId)
        mTempFragment = FragmentStackManager.getInstance().getCertainFragment(mCurrentFragmentTag).fragment
        mNavigationView.setCheckedItem(R.id.item_home)

        showBottomBar()
    }

    @Synchronized
    private fun setClickedMenuItem(menuItem: MenuItem){
        mClickedMenuItem = menuItem
    }

    @Synchronized
    private fun getClickedMenuItem() : MenuItem ?{
        return mClickedMenuItem
    }

    private fun closeDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        setClickedMenuItem(p0)
        closeDrawer()
        return true
    }

    override fun onBackPressed() {
        val amount = supportFragmentManager.backStackEntryCount
        if(amount > 0){
            back2Home()
        } else {
            if(mDrawerLayout.isDrawerOpen(mNavigationView)){
                closeDrawer()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        clearFindViewByIdCache()
        FragmentStackManager.getInstance().popAllFragments()
        super.onDestroy()
    }


}