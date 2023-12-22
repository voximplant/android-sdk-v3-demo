/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.catalog.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.voximplant.demos.sdk.feature.catalog.CatalogRoute

const val catalogRoute = "catalog_route"

fun NavController.navigateToCatalog(navOptions: NavOptions? = null) {
    this.navigate(catalogRoute, navOptions)
}

fun NavGraphBuilder.catalogScreen(
    onLoginClick: () -> Unit,
    onModuleClick: (String) -> Unit,
) {
    composable(route = catalogRoute) {
        CatalogRoute(
            onLoginClick = onLoginClick,
            onModuleClick = onModuleClick,
        )
    }
}
