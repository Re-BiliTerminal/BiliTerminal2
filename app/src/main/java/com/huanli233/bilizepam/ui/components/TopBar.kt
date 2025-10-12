package com.huanli233.bilizepam.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData

enum class TopBarState {
    MENU,
    PAGE
}

@Composable
fun TopBar(
    title: String,
    state: TopBarState,
    time: String,
    modifier: Modifier = Modifier,
    isMenuScreen: Boolean,
    roundMode: Boolean = if (LocalInspectionMode.current) false else LocalData.settings.uiSettings.roundMode
) {
    if (roundMode) {
        TopBarRound(
            modifier = modifier,
            title = title,
            state = state,
            time = time,
            isMenuScreen = isMenuScreen
        )
    } else {
        TopBarDefault(
            modifier = modifier,
            title = title,
            state = state,
            time = time,
            isMenuScreen = isMenuScreen
        )
    }
}

@Composable
private fun TopBarRound(
    modifier: Modifier = Modifier,
    title: String,
    state: TopBarState,
    time: String,
    isMenuScreen: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTextClock(
            time = time,
            roundMode = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
        TitleSwitcher(
            title = title,
            state = state,
            isMenuScreen = isMenuScreen,
            roundMode = true,
            modifier = Modifier.padding(top = 2.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 2.dp),
            thickness = 1.dp,
            color = LocalContentColor.current.copy(alpha = 0.12f)
        )
    }
}

@Composable
private fun TopBarDefault(
    modifier: Modifier = Modifier,
    title: String,
    state: TopBarState,
    time: String,
    isMenuScreen: Boolean
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val (titleRef, clockRef) = createRefs()
        val guideline = createGuidelineFromStart(0.7f)

        TitleSwitcher(
            title = title,
            state = state,
            isMenuScreen = isMenuScreen,
            roundMode = false,
            modifier = Modifier.constrainAs(titleRef) {
                start.linkTo(parent.start)
                end.linkTo(guideline, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )
        AppTextClock(
            time = time,
            roundMode = false,
            modifier = Modifier.constrainAs(clockRef) {
                end.linkTo(parent.end, margin = 16.dp)
                top.linkTo(parent.top, margin = 4.dp)
                bottom.linkTo(parent.bottom, margin = 2.dp)
            }
        )
    }
}

@Composable
private fun AppTextClock(
    time: String,
    roundMode: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (LocalInspectionMode.current) "12:08" else time,
        modifier = modifier,
        color = if (roundMode) LocalContentColor.current.copy(alpha = 0.85f) else Color.Unspecified,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = if (roundMode) TextAlign.Center else TextAlign.End
    )
}

@Composable
private fun TitleSwitcher(
    title: String,
    state: TopBarState,
    isMenuScreen: Boolean,
    roundMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetState = Pair(title, state)

    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            if (targetState.second == TopBarState.MENU && initialState.second == TopBarState.PAGE) {
                (slideInVertically { height -> -height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
            } else if (targetState.second == TopBarState.PAGE && initialState.second == TopBarState.MENU) {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
            } else {
                fadeIn()
                    .togetherWith(fadeOut())
            }.using(
                SizeTransform(clip = false)
            )
        }, label = "TitleSwitcher"
    ) { (currentTitle, currentState) ->
        val iconRes = if (isMenuScreen) {
            when (currentState) {
                TopBarState.MENU -> R.drawable.icon_keyboard_arrow_left
                TopBarState.PAGE -> R.drawable.icon_keyboard_arrow_down
            }
        } else {
            R.drawable.icon_keyboard_arrow_left
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (roundMode) Arrangement.Center else Arrangement.Start,
            modifier = if (roundMode) Modifier else Modifier.padding(vertical = 8.dp).padding(start = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = if (isMenuScreen) "Toggle Menu" else "Back",
                modifier = if (!isMenuScreen) {
                    Modifier
                        .clip(CircleShape)
                } else {
                    Modifier
                }
            )

            Text(
                text = if (LocalInspectionMode.current && currentTitle.isEmpty()) "Page Name" else currentTitle,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = if (roundMode) 15.sp else 13.sp,
                textAlign = if (roundMode) TextAlign.Center else TextAlign.Start
            )
        }
    }
}
