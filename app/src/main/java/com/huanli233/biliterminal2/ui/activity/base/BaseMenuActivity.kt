package com.huanli233.biliterminal2.ui.activity.base

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.ui.fragment.menu.MenuFragment

abstract class BaseMenuActivity : BaseActivity() {

    private lateinit var contentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_fragment_container)

        val contentFragmentTag = onCreateFragment(null)::class.qualifiedName

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                contentFragment = onCreateFragment(savedInstanceState)
                add(R.id.fragment_container, contentFragment, contentFragmentTag)
            }
        } else {
            contentFragment = supportFragmentManager.findFragmentByTag(contentFragmentTag)
                ?: throw IllegalStateException("Content fragment not found")
        }

        setupTopbar()
    }

    abstract fun onCreateFragment(savedInstanceState: Bundle?): Fragment

    abstract fun getMenuName(): String

    override fun setupTopbar() {
        super.setupTopbar()
        val topBarView = topBar ?: return

        topBarView.setBackIconVisible(true, R.drawable.icon_keyboard_arrow_down)

        topBarView.setTitle(getMenuName())

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is MenuFragment) {
                topBar?.setTitle(getString(R.string.menu))
                topBar?.setBackIconVisible(true, R.drawable.icon_keyboard_arrow_left)
            } else {
                topBar?.setTitle(getMenuName())
                topBar?.setBackIconVisible(true, R.drawable.icon_keyboard_arrow_down)
            }
        }

        topBarView.setOnClickListener {
            onTopbarClicked()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private val menuFragment by lazy {
        MenuFragment()
    }

    override fun onTopbarClicked() {
        val menuFragmentTag = menuFragment::class.qualifiedName
        if (supportFragmentManager.findFragmentByTag(menuFragmentTag) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                if (LocalData.settings.theme.animationsEnabled) {
                    setCustomAnimations(
                        R.anim.slide_in_from_top,
                        R.anim.slide_out_to_bottom,
                        R.anim.slide_in_from_bottom,
                        R.anim.slide_out_to_top
                    )
                }
                replace(R.id.fragment_container, menuFragment, menuFragmentTag)
                addToBackStack(null)
            }
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}