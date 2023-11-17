package com.voximplant.sdk3demo.feature.catalog.navigation

import CatalogRoute
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val catalogRoute = "catalog_route"

fun NavController.navigateToCatalog(navOptions: NavOptions? = null) {
    this.navigate(catalogRoute, navOptions)
}

fun NavGraphBuilder.catalogScreen(
    onModuleClick: (String) -> Unit,
) {
    composable(route = catalogRoute) {
        CatalogRoute(onModuleClick)
    }
}
