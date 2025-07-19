package co.edu.udea.compumovil.gr07_20251.udeafood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "unified_login") {

        composable("unified_login") {
            UnifiedLoginScreen(navController)
        }

        composable("store_register") {
            StoreRegisterScreen(navController)
        }

        composable("client_register") {
            ClientRegisterScreen(navController)
        }

        composable("map_picker") {
            MapPickerScreen(navController)
        }

        composable("store_catalog/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            StoreCatalogScreen(storeId = storeId, navController = navController)
        }

        composable("store_dashboard/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")
            if (storeId != null) {
                StoreDashboardScreen(storeId = storeId, navController = navController)
            }
        }

        composable("email_login/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            EmailLoginScreen(navController, type)
        }

        composable("map") {
            MapScreen(
                onAccountClick = { userType ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val uid = currentUser?.uid

                    if (uid != null) {
                        if (userType == "store") {
                            navController.navigate("store_dashboard/$uid")
                        } else {
                            navController.navigate("client_profile/$uid")
                        }
                    }
                },
                navController = navController
            )
        }

        composable("add_product/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            AddProductScreen(storeId = storeId, navController = navController)
        }

        composable("store_products") {
            StoreProductManagerScreen(navController)
        }

        composable("edit_product/{storeId}/{productId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductScreen(storeId, productId, navController)
        }

        composable("client_profile/{clientId}") { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable
            ClientProfileScreen(clientId, navController)
        }

        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(productId = productId, navController = navController)
        }

        composable("store_products/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            StoreProductListScreen(storeId = storeId, navController = navController)
        }

        composable("add_review/{storeId}/{productId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            AddReviewScreen(productId = productId, storeId = storeId, navController = navController)
        }

        composable("store_reviews") {
            StoreReviewManagerScreen()
        }

        composable("product_reviews/{storeId}/{productId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductReviewsScreen(productId = productId, storeId = storeId, navController = navController)
        }

        composable("favorites") {
            FavoriteScreen(navController)
        }

        composable("edit_store/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            EditStoreScreen(storeId, navController)
        }

        composable("global_catalog") {
            GlobalCatalogScreen(navController = navController)
        }

    }
}


