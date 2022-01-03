package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arsylk.mammonsmite.model.common.NavTab

@Composable
fun <N : NavTab> BottomTabNavigation(
    modifier: Modifier = Modifier,
    tabs: Array<N>,
    selected: N,
    alwaysShowLabel: Boolean = true,
    onTabClick: (tab: N) -> Unit,
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        for (tab in tabs) {
            BottomNavigationItem(
                selected = tab == selected,
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
                onClick = { onTabClick.invoke(tab) },
                alwaysShowLabel = alwaysShowLabel,
            )
        }
    }
}