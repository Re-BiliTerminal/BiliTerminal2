package com.huanli233.biliterminal2.ui.fragment.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.MultiTypeAdapter
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.menu.MenuConfigManager
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment
import com.huanli233.biliterminal2.ui.utils.recyclerview.defaultLayoutManager
import com.huanli233.biliterminal2.utils.multitype.register

class MenuFragment(
    private val onDismiss: () -> Unit
): BaseFragment() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = requireContext().defaultLayoutManager
        recyclerView.adapter = MultiTypeAdapter(
            MenuConfigManager.readMenuConfig().menuItems
        ).register {
            +MenuItemViewDelegate {
                onDismiss()
                context?.startActivity(Intent(context, it))
            }
        }
    }
}