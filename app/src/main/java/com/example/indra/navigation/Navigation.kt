package com.example.indra.navigation





import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.indra.screen.AssessmentView
import com.example.indra.screen.CommunityScreen
import com.example.indra.screen.DashboardScreen
import com.example.indra.screen.HistoryView
import com.example.indra.screen.LearnHubView
import com.example.indra.screen.MyPropertiesScreen
import com.example.indra.screen.ProfileScreen
import com.example.indra.screen.ReportCard
import com.example.indra.screen.ReportView
import com.example.indra.screen.ServicesScreen
import com.example.indra.screen.SettingsScreen


// Using a sealed class to define destinations akin to a NavGraph.




// Define routes as constants for type safety and clarity.
object AppRoutes {
    const val DASHBOARD = "dashboard"
    const val ONBOARDING = "onboarding"
    const val ASSESS = "assess"
    const val COMMUNITY = "community"
    const val SERVICES = "Jal Sanchay Mitra"
    const val HISTORY = "history"
    const val LEARN_HUB = "learn_hub"
    const val REPORT = "report"
    const val DETAILED_REPORT = "detailed_report"
    const val REPORT_CARD = "report_card"
    const val PROFILE = "profile"
    const val MY_PROPERTIES = "my_properties"
    const val SETTINGS = "settings"
    const val MY_HOUSE = "my_house"

}

/**
 * A sealed class defining all screen destinations in the app.
 * This class serves as a data model for navigation items and their metadata.
 * The actual navigation logic and Composable content are handled in the NavHost.
 */
sealed class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Main screens accessible from the bottom bar
    data object MyHouse : AppDestination(AppRoutes.MY_HOUSE, "Home", Icons.Default.Home)
    data object Dashboard : AppDestination(AppRoutes.DASHBOARD, "Dashboard", Icons.Default.Dashboard)
    data object History : AppDestination(AppRoutes.HISTORY, "History", Icons.Default.History)
    data object LearnHub : AppDestination(AppRoutes.LEARN_HUB, "Learn Hub", Icons.Default.MenuBook)
    data object Community : AppDestination(AppRoutes.COMMUNITY, "Community", Icons.Default.People)

    // Other destinations
    data object Assess : AppDestination(AppRoutes.ASSESS, "Assess", Icons.Default.Calculate)
    data object Services : AppDestination(AppRoutes.SERVICES, "Services", Icons.Default.Build)
    data object Report : AppDestination(AppRoutes.REPORT, "Report", Icons.Default.Receipt)
    data object ReportCardDest : AppDestination(AppRoutes.REPORT_CARD, "Report Card", Icons.Default.Assignment)

    // Screens accessible from the navigation drawer
    data object Profile : AppDestination(AppRoutes.PROFILE, "My Profile", Icons.Default.Person)
    data object MyProperties : AppDestination(AppRoutes.MY_PROPERTIES, "My Properties", Icons.Default.HomeWork)
    data object Settings : AppDestination(AppRoutes.SETTINGS, "Settings", Icons.Default.Settings)

}