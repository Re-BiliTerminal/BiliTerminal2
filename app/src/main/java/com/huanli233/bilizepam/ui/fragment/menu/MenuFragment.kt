package com.huanli233.bilizepam.ui.fragment.menu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.menu.MenuConfigManager
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.fragment.base.BaseFragment
import com.huanli233.bilizepam.ui.utils.animationsEnabled
import com.huanli233.bilizepam.ui.utils.hikage.extension.transitionNameCompat
import com.huanli233.bilizepam.ui.utils.makeSceneTransitionAnimation
import com.huanli233.bilizepam.ui.utils.recyclerview.defaultLayoutManager
import com.huanli233.hikage.recyclerview.initMultiType
import com.huanli233.hikage.recyclerview.register
import kotlinx.coroutines.launch

class MenuFragment: BaseFragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.initMultiType {
            +MenuItemDelegate { it, view ->
                if (!it.activityClass.isInstance(requireActivity())) {
                    view.transitionNameCompat = "menu_item"
                    val intent = Intent(context, it.activityClass).apply {
                        putExtra("transition_name", "menu_item")
                    }
//                    val activity = activity
//                    if (animationsEnabled && activity != null) {
//                        (activity as? BaseActivity)?.setupSharedElementTransitionExit()
//                        ContextCompat.startActivity(requireContext(), intent,
//                            activity.makeSceneTransitionAnimation(view, "menu_item")
//                                .toBundle()
//                        )
//                    } else {
                        context?.startActivity(intent)
//                    }
                    if (!it.notMenuActivity) {
                        requireActivity().finish()
                    }
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
        lifecycleScope.launch {
            AccountManager.repository.activeAccount.collect {
                (recyclerView.adapter as? MultiTypeAdapter)?.items = MenuConfigManager.readMenuConfig().menuItems
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}