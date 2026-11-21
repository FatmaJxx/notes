package com.example.notes.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun NotesNavigationSuiteScaffold(
    navigationSuiteItems: NotesNavigationSuiteScope.() -> Unit,
    layoutType: NavigationSuiteType,
    content: @Composable () -> Unit
) {

    val navigationSuiteItemColors = NavigationSuiteItemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )

    NavigationSuiteScaffoldLayout(
        layoutType = layoutType,
        navigationSuite = {
            if (layoutType != NavigationSuiteType.None) {
                if (layoutType == NavigationSuiteType.NavigationBar) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            NavigationSuite(layoutType = layoutType) {
                                NotesNavigationSuiteScope(
                                    navigationSuiteScope = this,
                                    navigationSuiteItemColors = navigationSuiteItemColors
                                ).run(navigationSuiteItems)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        NavigationSuite(layoutType = layoutType) {
                            NotesNavigationSuiteScope(
                                navigationSuiteScope = this,
                                navigationSuiteItemColors = navigationSuiteItemColors
                            ).run(navigationSuiteItems)
                        }
                    }
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            content()
        }
    }
}

class NotesNavigationSuiteScope internal constructor(
    private val navigationSuiteScope: NavigationSuiteScope,
    private val navigationSuiteItemColors: NavigationSuiteItemColors,
) {
    fun item(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: @Composable () -> Unit,
    ) = navigationSuiteScope.item(
        selected = selected,
        onClick = onClick,
        icon = {
            icon()
        },
        colors = navigationSuiteItemColors,
        modifier = modifier
    )
}
