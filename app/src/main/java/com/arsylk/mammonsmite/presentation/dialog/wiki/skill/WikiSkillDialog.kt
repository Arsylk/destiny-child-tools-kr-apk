package com.arsylk.mammonsmite.presentation.dialog.wiki.skill

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arsylk.mammonsmite.model.common.collectAsState
import com.arsylk.mammonsmite.model.destinychild.FullSkill
import com.arsylk.mammonsmite.presentation.AppMaterialTheme
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.dialog.NavigableDialog
import com.arsylk.mammonsmite.presentation.dialog.wiki.buff.BuffContent
import com.arsylk.mammonsmite.presentation.dialog.wiki.buff.WikiBuffDialog
import com.arsylk.mammonsmite.presentation.dialog.wiki.buff.WikiBuffViewModel
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

object WikiSkillDialog : NavigableDialog {
    override val route = "/wiki/skill/{idx}"
    override val label = "Skill"
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
        val viewModel by viewModel<WikiSkillViewModel> { parametersOf(idx) }

        WikiSkillDialog(viewModel)
    }
}

@Composable
fun WikiSkillDialog(viewModel: WikiSkillViewModel) {
    val uiSkill by viewModel.skill.collectAsState()
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(2.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(16.dp))
    ) {
        BoxWithConstraints {
            UiResultBox(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(min = 100.dp, max = maxHeight * 0.85f),
                uiResult = uiSkill,
            ) { skill ->
                val text = remember(skill) { viewModel.serializeSkill(skill) }
                SkillContent(skill, text)
            }
        }
    }
}

@Composable
internal fun SkillContent(skill: FullSkill, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = text,
            fontFamily = AppMaterialTheme.ConsoleFontFamily,
            style = MaterialTheme.typography.caption,
        )
    }
}