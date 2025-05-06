package com.huanli233.biliterminal2.ui.activity.base

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.fragment.menu.MenuFragment

abstract class BaseMenuActivity : BaseActivity() {

    private lateinit var contentFragment: Fragment

    private var inMenu: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_item)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                contentFragment = onCreateFragment(savedInstanceState)
                add(R.id.fragment_container, contentFragment, contentFragment::class.qualifiedName)
            }
        } else {
            contentFragment = supportFragmentManager.findFragmentByTag(
                onCreateFragment(null)::class.qualifiedName
            ) ?: throw IllegalStateException("content fragment not found")
        }

        setupTopbar()
    }

    abstract fun onCreateFragment(savedInstanceState: Bundle?): Fragment

    abstract fun getMenuName(): String

    override fun setupTopbar() {
        super.setupTopbar()
        val topBarView = topBar ?: return

        topBarView.titleTextView.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(this, R.drawable.icon_keyboard_arrow_down), null, null, null
        )

        topBarView.setTitle(getMenuName())

        supportFragmentManager.addOnBackStackChangedListener {
            if (inMenu) {
                topBar?.setTitle(getString(R.string.menu))
                topBar?.titleTextView?.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.icon_keyboard_arrow_left), null, null, null
                )
            } else {
                topBar?.setTitle(getMenuName())
                topBar?.titleTextView?.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.icon_keyboard_arrow_down), null, null, null
                )
            }
        }

        topBarView.setOnClickListener {
            onTopbarClicked()
        }
    }

    private val menuFragment by lazy {
        MenuFragment {
            inMenu = false
        }
    }

    override fun onTopbarClicked() {
        if (supportFragmentManager.findFragmentByTag(menuFragment::class.qualifiedName) == null) {
            inMenu = true

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(
                    R.anim.slide_in_top,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_top
                )
                replace(R.id.fragment_container, menuFragment, menuFragment::class.qualifiedName)

                addToBackStack(null)
            }
        } else {
            inMenu = false
            supportFragmentManager.popBackStack()
        }
    }
}