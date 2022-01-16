package com.arsylk.mammonsmite.presentation.screen.wiki.character

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.model.common.collectAsState
import com.arsylk.mammonsmite.model.destinychild.FullCharacter
import com.arsylk.mammonsmite.presentation.Navigable.Companion.putArg
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.SurfaceBox
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.domain.WithAlpha
import com.arsylk.mammonsmite.domain.noRippleClickable
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.destinychild.SkillWithBuffs
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.naturalOrder
import com.arsylk.mammonsmite.model.destinychild.ViewIdx.Companion.preferred

object WikiCharacterScreen : NavigableScreen {
    override val route = "/wiki/character/{idx}"
    override val label = "Wiki Character"
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
        val viewModel by viewModel<WikiCharacterViewModel> { parametersOf(idx) }

        WikiCharacterScreen(viewModel)
    }
}

@Composable
fun WikiCharacterScreen(viewModel: WikiCharacterViewModel) {
    val character by viewModel.character.collectAsState()
    SurfaceBox {
        UiResultBox(
            modifier = Modifier.fillMaxSize(),
            uiResult = character,
        ) { char ->
            CharacterContents(char)
        }
    }
}

@Composable
internal fun CharacterContents(char: FullCharacter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        var selectedViewIdx by remember {
            mutableStateOf(char.viewIdxNames.keys.preferred())
        }
        CharacterNameRow(char, selectedViewIdx)
        CharacterIconsRow(char, selectedViewIdx) { selectedViewIdx = it }
        Spacer(Modifier.height(8.dp))
        SkillNode(
            label = "Default",
            normal = char.baseSkills.default,
            ignited = char.ignitedSkills?.default,
        )
        SkillNode(
            label = "Normal",
            normal = char.baseSkills.normal,
            ignited = char.ignitedSkills?.normal,
        )
        SkillNode(
            label = "Slide",
            normal = char.baseSkills.slide,
            ignited = char.ignitedSkills?.slide,
        )
        SkillNode(
            label = "Drive",
            normal = char.baseSkills.drive,
            ignited = char.ignitedSkills?.drive,
        )
        SkillNode(
            label = "Leader",
            normal = char.baseSkills.leader,
            ignited = char.ignitedSkills?.leader,
        )
    }
}

@Composable
internal fun CharacterNameRow(
    char: FullCharacter,
    selectedViewIdx: ViewIdx?,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        Column {
            Text(
                text = char.viewIdxNames[selectedViewIdx] ?: "<Unknown>",
                style = MaterialTheme.typography.h6,
            )
            WithAlpha(alpha = ContentAlpha.medium) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = char.data.idx,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle2
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = char.data.koreanName,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        }
    }
}

@Composable
internal fun CharacterIconsRow(
    char: FullCharacter,
    selectedViewIdx: ViewIdx?,
    onSelect: (ViewIdx) -> Unit,
) {
    val iconSize = 64.dp
    val padding = 8.dp

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = padding)
    ) {
        val set = char.viewIdxNames.keys.naturalOrder()
        set.forEachIndexed { i, viewIdx ->
            val alpha by animateFloatAsState(
                if (selectedViewIdx == viewIdx) ContentAlpha.high else ContentAlpha.disabled
            )
            Column(
                modifier = Modifier
                    .width(iconSize)
                    .noRippleClickable { onSelect.invoke(viewIdx) }
            ) {
                Box(modifier = Modifier.size(iconSize)) {
                    Image(
                        painter = rememberImagePainter(data = viewIdx.iconUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Image(
                        painter = rememberImagePainter(char.data.attribute.frameRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    val starSize = 18.dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(starSize)
                    ) {
                        repeat(char.data.stars) { i ->
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(starSize)
                                    .offset(x = (starSize / 2) * i)
                            )
                        }
                    }
                }
                WithAlpha(alpha = alpha) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = viewIdx.string,
                        maxLines = 1,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
            if (i < set.size - 1) Spacer(Modifier.width(padding))
        }
    }
}

@Composable
internal fun SkillNode(
    label: String,
    normal: SkillWithBuffs,
    ignited: SkillWithBuffs?,
) {
    var showIgnited by remember { mutableStateOf(false) }
    val skill = if (showIgnited) ignited ?: normal else normal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .noRippleClickable { showIgnited = ignited != null && !showIgnited }
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                use(skill.name) {
                    WithAlpha(alpha = ContentAlpha.high) {
                        Text(
                            text = it,
                            color = if (showIgnited) Color.Red else Color.Unspecified,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                WithAlpha(alpha = if (skill.name != null) ContentAlpha.medium else ContentAlpha.high) {
                    Text(
                        text = "$label - ${skill.data?.idx}",
                        color = if (showIgnited && skill.name == null) Color.Red else Color.Unspecified,
                        style = MaterialTheme.typography.subtitle2,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val set = skill.buffs
                set.forEachIndexed { i, buff ->
                    var show by remember { mutableStateOf(true) }
                    val painter = rememberImagePainter(buff.data.iconUrl) {
                        listener(
                            onError = { _, _ -> show = false }
                        )
                    }

                    if (show) {
                        Image(
                            modifier = Modifier
                                .size(28.dp),
                            painter = painter,
                            contentDescription = null,
                        )
                        if (i < set.size - 1) Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }
        use(skill.contents) {
            Text(
                text = it,
                style = MaterialTheme.typography.subtitle2,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}