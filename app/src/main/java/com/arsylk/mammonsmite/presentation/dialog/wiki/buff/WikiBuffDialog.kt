package com.arsylk.mammonsmite.presentation.dialog.wiki.buff

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.domain.WithAlpha
import com.arsylk.mammonsmite.domain.orUnknown
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.common.collectAsState
import com.arsylk.mammonsmite.model.destinychild.FullBuff
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.dialog.NavigableDialog
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

object WikiBuffDialog : NavigableDialog {
    override val route = "/wiki/buff/{idx}"
    override val label = "Buff"
    override val args = listOf(
        navArgument("idx") { type = NavType.StringType }
    )

    fun navigate(nav: Navigator, idx: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        val route = route.putArg("idx", idx)
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        val idx = entry.arguments?.getString("idx")!!
        val viewModel by viewModel<WikiBuffViewModel> { parametersOf(idx) }

        WikiBuffDialog(viewModel)
    }
}

@Composable
fun WikiBuffDialog(viewModel: WikiBuffViewModel) {
    val uiBuff by viewModel.buff.collectAsState()
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(16.dp))
    ) {
        UiResultBox(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .defaultMinSize(minHeight = 100.dp)
                .wrapContentWidth(),
            uiResult = uiBuff
        ) { buff ->
            BuffContent(buff)
        }
    }
}

@Composable
internal fun BuffContent(buff: FullBuff) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                modifier = Modifier.size(48.dp),
                painter = rememberImagePainter(data = buff.data.iconUrl),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = buff.name.orUnknown(),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Column {
            WithAlpha(alpha = ContentAlpha.high) {
                use(buff.contents) { contents ->
                    Text(
                        text = contents,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
            WithAlpha(alpha = ContentAlpha.medium) {
                Text(
                    text = buff.data.idx,
                    style = MaterialTheme.typography.subtitle2,
                )
                Text(
                    text = buff.data.logic,
                    style = MaterialTheme.typography.subtitle2,
                )
                Text(
                    text = "Rank: ${buff.data.rank}",
                    style = MaterialTheme.typography.caption,
                )
                Text(
                    text = "Type: ${buff.data.type}",
                    style = MaterialTheme.typography.caption,
                )
                val categories = remember(buff.data) {
                    buff.data.categories
                        .filter { it != 0 }
                        .sorted()
                        .joinToString(separator = ", ") { buff.categories[it] ?: it.toString() }
                }
                if (categories.isNotBlank()) Text(
                    text = "Categories: $categories",
                    style = MaterialTheme.typography.caption,
                )
            }
        }
    }
}