package com.warchaser.titlebar

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.warchaser.titlebar.util.DrawableUtil
import com.warchaser.titlebar.util.StatusBarUtil

/**
 * Created by jack on 2017/9/14.
 */
class TitleBar : RelativeLayout {
    private var mContext : Context ? = null
    private var mOnBackListener: OnBackListener? = null
    private var mOnEndFunctionalButtonClickListener : OnEndFunctionalButtonClickListener? = null

    private var mTheme : Theme = Theme.WHITE

    private var mEndFunctionalBtn : Button ? = null
    private var mStatusBar : View ? = null
    private var mImBack : ImageView? = null
    private var mTitleTxt : TextView? = null
    private var mBackLayout : RelativeLayout? = null
    private var mEndLayout : RelativeLayout? = null
    private var mEndFunctionalLayout : RelativeLayout? = null

    private var mBackGroundColor : Int = -1

    constructor(context: Context) : super(context)
    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
            LayoutInflater.from(context).inflate(R.layout.titlebar_for_5, this)
        } else {
            LayoutInflater.from(context).inflate(R.layout.titlebar, this)
        }

        mEndFunctionalBtn = findViewById(R.id.mEndFunctionalBtn)
        mStatusBar = findViewById(R.id.mStatusBar)
        mImBack = findViewById(R.id.mImBack)
        mTitleTxt = findViewById(R.id.mTitleTxt)
        mBackLayout = findViewById(R.id.mBackLayout)
        mEndLayout = findViewById(R.id.mEndLayout)
        mEndFunctionalLayout = findViewById(R.id.mEndFunctionalLayout)

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.TitleBar)

        mTheme = Theme.valueOf(a.getInt(R.styleable.TitleBar_themeCus, Theme.WHITE.value))
        mBackGroundColor = a.getColor(R.styleable.TitleBar_backGroundColor, mTheme.value)

        val titleText = a.getString(R.styleable.TitleBar_titleText)
        if(!TextUtils.isEmpty(titleText)){
            setTitle(titleText)
        }

        val backEndText = a.getString(R.styleable.TitleBar_backEndText)
        if(!TextUtils.isEmpty(backEndText)){
            setEndFunctionButtonText(backEndText)
        }

        a.recycle()

        initialize()
    }

    private fun initialize(){

        mEndFunctionalBtn?.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                if(mOnEndFunctionalButtonClickListener != null){
                    mOnEndFunctionalButtonClickListener!!.onClick()
                }
            }
        })

        var isDark = true

        when (mTheme) {
            Theme.WHITE -> {

                setBackgroundColor(getContextColor(R.color.color_fafafa))

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
                    val layoutParams = mStatusBar?.layoutParams
                    layoutParams?.height = StatusBarUtil.getStatusBarHeight(context)
                    mStatusBar?.layoutParams = layoutParams
                    mStatusBar?.visibility = View.VISIBLE
                }

                if(isInEditMode) {
                    return
                }

                StatusBarUtil.setDarkStatusIcon(isDark, mContext as Activity)
            }
            Theme.BLUE -> {
                setTitleColor(R.color.white)
                setEndFunctuionButtonTextColor(R.color.white)
                setBackgroundResource(R.drawable.titlebar_bg)
                mImBack?.setImageResource(R.mipmap.back_white)

                isDark = false

                if(isInEditMode) {
                    return
                }

                StatusBarUtil.setDarkStatusIcon(isDark, mContext as Activity)
            }
            Theme.TRANSPARENT -> {
                setTitleColor(R.color.white)
                setEndFunctuionButtonTextColor(R.color.white)
                setBackgroundColor(getContextColor(R.color.transparent))
                mImBack?.setImageResource(R.mipmap.back_white)

                isDark = false

            }
            Theme.BLACK->{
                setBackgroundColor(getContextColor(R.color.color_fafafa))
            }
        }

        if(mBackGroundColor != -1){
            setBackgroundColor(mBackGroundColor)
        }

//        (mContext as BaseActivity).setDarkStatusIcon(isDark)

    }

    /**
     * 设置标题
    */
    fun setTitle(title: String) {
        mTitleTxt?.text = title
    }

    fun setTitle(titleResource: Int) {
        mTitleTxt?.setText(titleResource)
    }

    /**
     * 设置标题颜色
     * */
    fun setTitleColor(resId : Int){
        mTitleTxt?.setTextColor(getContextColor(resId))
    }

    /**
     * 设置start的显示和隐藏
     */
    fun setStartVisible(isVisible: Boolean) {
        if (isVisible) {
            mBackLayout?.visibility = View.VISIBLE
        } else {
            mBackLayout?.visibility = View.INVISIBLE
        }
    }

    /**
     * 设置end的显示和隐藏
     */
    fun setEndVisible(isVisible: Boolean) {
        if (isVisible) {
            mEndLayout?.visibility = View.VISIBLE
        } else {
            mEndLayout?.visibility = View.INVISIBLE
        }
    }

    /**
     * 右部功能按键显隐
     * */
    fun setEndFunctionalButtonVisible(isVisible: Boolean){
        if(isVisible){
            mEndFunctionalLayout?.visibility = View.VISIBLE
        } else {
            mEndFunctionalLayout?.visibility = View.GONE
        }
    }

    /**
     * 右部功能按钮文字
     * */
    fun setEndFunctionButtonText(str : String){
        mEndFunctionalBtn?.text = str
    }

    /**
     * 右部功能按钮文字
     * */
    fun setEndFunctionButtonText(resId : Int){
        mEndFunctionalBtn?.text = context.resources.getString(resId)
    }

    /**
     * 右部功能按钮文字颜色
     * */
    fun setEndFunctuionButtonTextColor(resId : Int){
        mEndFunctionalBtn?.setTextColor(getContextColor(resId))
    }

    /**
     * 设置左边回退按钮具体动作的方法
     * */
    fun setOnBackListener(listener: OnBackListener) {
        mOnBackListener = listener
    }

    fun setOnEndFunctionalButtonClickListener(listener: OnEndFunctionalButtonClickListener){
        mOnEndFunctionalButtonClickListener = listener
    }

    private fun getContextColor(resId: Int) : Int {
        return DrawableUtil.getContextColor(resId, mContext)
//        } else {
//            DrawableUtil.getContextColor(resId)
//        }
    }

//    /**
//     * 设置左侧回退按钮背景
//     * */
//    fun setBackBackgroud(resId: Int){
//        mBackGuide.setBackgroundResource(resId)
//    }

    fun getViewHeight() : Int{
        return measuredHeight
    }

    /**
     * 左上角回退按钮需要实现的动作的接口
     * */
    interface OnBackListener {
        fun onBackClick()
    }

    interface OnEndFunctionalButtonClickListener{
        fun onClick()
    }

    enum class Theme constructor(val value: Int) {
        WHITE(0), BLUE(1), TRANSPARENT(2), BLACK(3);

        companion object {

            //手写的从int到enum的转换函数
            fun valueOf(value: Int): Theme {
                when (value) {
                    0 -> return WHITE
                    1 -> return BLUE
                    2 -> return TRANSPARENT
                    3 -> return BLACK
                }
                return WHITE
            }
        }
    }

    fun destroy(){
        mOnBackListener = null
        mOnEndFunctionalButtonClickListener = null
        mEndFunctionalBtn = null
        mStatusBar = null
        mImBack = null
        mTitleTxt = null

        mBackLayout?.removeAllViewsInLayout()
        mBackLayout = null

        mEndLayout?.removeAllViewsInLayout()
        mEndLayout = null

        mEndFunctionalLayout?.removeAllViewsInLayout()
        mEndFunctionalLayout = null
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mBackLayout?.setOnClickListener {

            if (mOnBackListener != null) {
                mOnBackListener!!.onBackClick()
            } else {
                if (context is Activity) {
                    (context as Activity).finish()
                }
            }

        }
    }
}