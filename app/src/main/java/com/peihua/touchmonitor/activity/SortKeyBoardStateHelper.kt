package com.peihua.touchmonitor.activity

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.activity.ComponentActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.peihua8858.tools.utils.dLog

/**
 *
 * 监听键盘显示和隐藏
 * @date 2024/5/13 14:13
 **/
class SortKeyBoardStateHelper(
    private val activity: ComponentActivity,
    private val isImmersiveStatusBar: Boolean = false,
) : OnGlobalLayoutListener, DefaultLifecycleObserver, ViewTreeObserver.OnGlobalFocusChangeListener {

    private var wasOpened = false
    private var listener: ((isShow: Boolean) -> Unit)? = null
    private var mPopupWindow: PopupWindow? = null
    private val rootView = View(activity)

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        mPopupWindow = PopupWindow(activity)
        try {
            rootView.setLayoutParams(ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT))
            mPopupWindow?.setContentView(rootView)
            mPopupWindow?.softInputMode =
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            mPopupWindow?.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
            mPopupWindow?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            mPopupWindow?.isTouchable = false
        } catch (e: Exception) {
            dLog { TAG+"observerKeyboard failed: ${e.message}" }
        }
        checkSortInputMode(activity)
        activity.viewTreeObserver.addOnGlobalFocusChangeListener(this)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            val windowToken = activity.windowToken
            if (windowToken != null && mPopupWindow?.isShowing == false) {
                mPopupWindow?.showAtLocation(
                    activity.contentRoot,
                    Gravity.NO_GRAVITY,
                    0,
                    0
                )
            }
        }
    }

    override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
        dLog { TAG+"onGlobalFocusChanged: oldFocus=$oldFocus, newFocus=$newFocus" }
        onWindowFocusChanged(newFocus?.context == activity)
    }

    fun setOnKeyBoardStateListener(listener: (isShow: Boolean) -> Unit) {
        this.listener = listener
    }

    override fun onGlobalLayout() {
        var displayHeight = activity.window.decorView.height
        if (isImmersiveStatusBar) {
            val statusBarHeight = activity.statusBarHeight
            displayHeight -= statusBarHeight
        }
        //监听键盘弹起
        val isOpen = displayHeight > rootView.height
        if (isOpen == wasOpened) {
            //keyboard state has not changed
            return
        }
        wasOpened = isOpen
        listener?.invoke(isOpen)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.viewTreeObserver.removeOnGlobalFocusChangeListener(this)
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        mPopupWindow?.dismiss()
        activity.lifecycle.removeObserver(this)
    }

    fun isShowKeyBoard(): Boolean {
        return activity.isKeyboardVisible
    }


    companion object {
        private const val KEYBOARD_MIN_HEIGHT_RATIO = 0.15
        const val TAG = "SortKeyBoardStateHelper"
        val Activity.statusBarHeight: Int
            get() {
                val resourceId= resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                   return resources.getDimensionPixelSize(resourceId)
                }
                return 0
            }
        val Activity.viewTreeObserver: ViewTreeObserver
            get() = activityRoot.viewTreeObserver
        val Activity.windowToken: IBinder?
            get() = window.decorView.windowToken

        @JvmStatic
        val Activity.isKeyboardVisible: Boolean
            get() {
                checkSortInputMode(this)
                val r = Rect()
                val activityRoot = activityRoot
                activityRoot.getWindowVisibleDisplayFrame(r)
                val location = IntArray(2)
                contentRoot.getLocationOnScreen(location)
                val screenHeight = activityRoot.rootView.height
                val heightDiff = screenHeight - r.height() - location[1]
                return heightDiff > screenHeight * KEYBOARD_MIN_HEIGHT_RATIO
            }

        val Activity.activityRoot: View
            get() = contentRoot.rootView
        val Activity.contentRoot: ViewGroup
            get() = findViewById(android.R.id.content)

        private fun checkSortInputMode(activity: Activity) {
            val softInputAdjust =
                activity.window.attributes.softInputMode and (WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
                        or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

            // fix for #37 and #38.
            // The window will not be resized in case of SOFT_INPUT_ADJUST_NOTHING
            val isNotAdjustNothing =
                softInputAdjust and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            require(isNotAdjustNothing) { "Parameter:activity window SoftInputMethod is SOFT_INPUT_ADJUST_NOTHING. In this case window will not be resized" }
        }
    }
}