package eu.kanade.presentation.library.anime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEach
import eu.kanade.presentation.components.BasicItem
import eu.kanade.presentation.components.CheckboxItem
import eu.kanade.presentation.components.HeadingItem
import eu.kanade.presentation.components.IconItem
import eu.kanade.presentation.components.RadioItem
import eu.kanade.presentation.components.SortItem
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.presentation.components.TriStateItem
import eu.kanade.presentation.library.LibraryColumnsDialog
import eu.kanade.presentation.util.collectAsState
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.library.anime.AnimeLibrarySettingsScreenModel
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.entries.TriStateFilter
import tachiyomi.domain.library.anime.model.AnimeLibrarySort
import tachiyomi.domain.library.anime.model.sort
import tachiyomi.domain.library.model.AnimeLibraryGroup
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.display
import tachiyomi.domain.library.service.LibraryPreferences

@Composable
fun AnimeLibrarySettingsDialog(
    onDismissRequest: () -> Unit,
    screenModel: AnimeLibrarySettingsScreenModel,
    category: Category,
    // AM (GU) -->
    hasCategories: Boolean,
    // <-- AM (GU)
) {
    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = listOf(
            stringResource(R.string.action_filter),
            stringResource(R.string.action_sort),
            stringResource(R.string.action_display),
            // AM (GU) -->
            stringResource(R.string.group),
            // <-- AM (GU)
        ),
    ) { contentPadding, page ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> FilterPage(
                    screenModel = screenModel,
                )
                1 -> SortPage(
                    category = category,
                    screenModel = screenModel,
                )
                2 -> DisplayPage(
                    category = category,
                    screenModel = screenModel,
                )
                // AM (GU) -->
                3 -> GroupPage(
                    screenModel = screenModel,
                    hasCategories = hasCategories,
                )
                // <-- AM (GU)
            }
        }
    }
}

@Composable
private fun ColumnScope.FilterPage(
    screenModel: AnimeLibrarySettingsScreenModel,
) {
    val filterDownloaded by screenModel.libraryPreferences.filterDownloadedAnime().collectAsState()
    val downloadedOnly by screenModel.preferences.downloadedOnly().collectAsState()
    TriStateItem(
        label = stringResource(R.string.label_downloaded),
        state = if (downloadedOnly) {
            TriStateFilter.ENABLED_IS
        } else {
            filterDownloaded
        },
        enabled = !downloadedOnly,
        onClick = { screenModel.toggleFilter(LibraryPreferences::filterDownloadedAnime) },
    )
    val filterUnseen by screenModel.libraryPreferences.filterUnseen().collectAsState()
    TriStateItem(
        label = stringResource(R.string.action_filter_unseen),
        state = filterUnseen,
        onClick = { screenModel.toggleFilter(LibraryPreferences::filterUnseen) },
    )
    val filterStarted by screenModel.libraryPreferences.filterStartedAnime().collectAsState()
    TriStateItem(
        label = stringResource(R.string.label_started),
        state = filterStarted,
        onClick = { screenModel.toggleFilter(LibraryPreferences::filterStartedAnime) },
    )
    val filterBookmarked by screenModel.libraryPreferences.filterBookmarkedAnime().collectAsState()
    TriStateItem(
        label = stringResource(R.string.action_filter_bookmarked),
        state = filterBookmarked,
        onClick = { screenModel.toggleFilter(LibraryPreferences::filterBookmarkedAnime) },
    )
    val filterFillermarked by screenModel.libraryPreferences.filterFillermarkedAnime().collectAsState()
    TriStateItem(
        label = stringResource(R.string.action_filter_fillermarked),
        state = filterFillermarked,
        onClick = { screenModel.toggleFilter(LibraryPreferences::filterFillermarkedAnime) },
    )
    val filterCompleted by screenModel.libraryPreferences.filterCompletedAnime().collectAsState()
    TriStateItem(
        label = stringResource(R.string.completed),
        state = filterCompleted,
        onClick = { screenModel.toggleFilter(LibraryPreferences::filterCompletedAnime) },
    )

    val trackServices = remember { screenModel.trackServices }
    when (trackServices.size) {
        0 -> {
            // No trackers
        }
        1 -> {
            val service = trackServices[0]
            val filterTracker by screenModel.libraryPreferences.filterTrackedAnime(service.id.toInt()).collectAsState()
            TriStateItem(
                label = stringResource(R.string.action_filter_tracked),
                state = filterTracker,
                onClick = { screenModel.toggleTracker(service.id.toInt()) },
            )
        }
        else -> {
            HeadingItem(R.string.action_filter_tracked)
            trackServices.map { service ->
                val filterTracker by screenModel.libraryPreferences.filterTrackedAnime(service.id.toInt()).collectAsState()
                TriStateItem(
                    label = stringResource(service.nameRes()),
                    state = filterTracker,
                    onClick = { screenModel.toggleTracker(service.id.toInt()) },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SortPage(
    category: Category,
    screenModel: AnimeLibrarySettingsScreenModel,
) {
    // AM (GU) -->
    val globalSortMode by screenModel.libraryPreferences.libraryAnimeSortingMode().collectAsState()
    val sortingMode = if (screenModel.grouping == AnimeLibraryGroup.BY_DEFAULT) {
        category.sort.type
    } else {
        globalSortMode.type
    }
    val sortDescending = if (screenModel.grouping == AnimeLibraryGroup.BY_DEFAULT) {
        category.sort.isAscending
    } else {
        globalSortMode.isAscending
    }.not()
    // <-- AM (GU)

    listOf(
        R.string.action_sort_alpha to AnimeLibrarySort.Type.Alphabetical,
        R.string.action_sort_total_episodes to AnimeLibrarySort.Type.TotalEpisodes,
        R.string.action_sort_last_seen to AnimeLibrarySort.Type.LastSeen,
        R.string.action_sort_last_anime_update to AnimeLibrarySort.Type.LastUpdate,
        R.string.action_sort_unseen_count to AnimeLibrarySort.Type.UnseenCount,
        R.string.action_sort_latest_episode to AnimeLibrarySort.Type.LatestEpisode,
        R.string.action_sort_episode_fetch_date to AnimeLibrarySort.Type.EpisodeFetchDate,
        R.string.action_sort_date_added to AnimeLibrarySort.Type.DateAdded,
        R.string.action_sort_airing_time to AnimeLibrarySort.Type.AiringTime,
    ).map { (titleRes, mode) ->
        SortItem(
            label = stringResource(titleRes),
            sortDescending = sortDescending.takeIf { sortingMode == mode },
            onClick = {
                val isTogglingDirection = sortingMode == mode
                val direction = when {
                    isTogglingDirection -> if (sortDescending) AnimeLibrarySort.Direction.Ascending else AnimeLibrarySort.Direction.Descending
                    else -> if (sortDescending) AnimeLibrarySort.Direction.Descending else AnimeLibrarySort.Direction.Ascending
                }
                screenModel.setSort(category, mode, direction)
            },
        )
    }
}

@Composable
private fun ColumnScope.DisplayPage(
    category: Category,
    screenModel: AnimeLibrarySettingsScreenModel,
) {
    val portraitColumns by screenModel.libraryPreferences.animePortraitColumns().collectAsState()
    val landscapeColumns by screenModel.libraryPreferences.animeLandscapeColumns().collectAsState()

    var showColumnsDialog by rememberSaveable { mutableStateOf(false) }
    if (showColumnsDialog) {
        LibraryColumnsDialog(
            initialPortrait = portraitColumns,
            initialLandscape = landscapeColumns,
            onDismissRequest = { showColumnsDialog = false },
            onValueChanged = { portrait, landscape ->
                screenModel.libraryPreferences.animePortraitColumns().set(portrait)
                screenModel.libraryPreferences.animeLandscapeColumns().set(landscape)
                showColumnsDialog = false
            },
        )
    }

    HeadingItem(R.string.action_display_mode)
    listOf(
        R.string.action_display_grid to LibraryDisplayMode.CompactGrid,
        R.string.action_display_comfortable_grid to LibraryDisplayMode.ComfortableGrid,
        R.string.action_display_cover_only_grid to LibraryDisplayMode.CoverOnlyGrid,
        R.string.action_display_list to LibraryDisplayMode.List,
    ).map { (titleRes, mode) ->
        RadioItem(
            label = stringResource(titleRes),
            selected = category.display == mode,
            onClick = { screenModel.setDisplayMode(category, mode) },
        )
    }

    if (category.display != LibraryDisplayMode.List) {
        BasicItem(
            label = stringResource(R.string.pref_library_columns),
            onClick = { showColumnsDialog = true },
        )
    }

    HeadingItem(R.string.overlay_header)
    val downloadBadge by screenModel.libraryPreferences.downloadBadge().collectAsState()
    CheckboxItem(
        label = stringResource(R.string.action_display_download_badge_anime),
        checked = downloadBadge,
        onClick = {
            screenModel.togglePreference(LibraryPreferences::downloadBadge)
        },
    )
    val localBadge by screenModel.libraryPreferences.localBadge().collectAsState()
    CheckboxItem(
        label = stringResource(R.string.action_display_local_badge),
        checked = localBadge,
        onClick = {
            screenModel.togglePreference(LibraryPreferences::localBadge)
        },
    )
    val languageBadge by screenModel.libraryPreferences.languageBadge().collectAsState()
    CheckboxItem(
        label = stringResource(R.string.action_display_language_badge),
        checked = languageBadge,
        onClick = {
            screenModel.togglePreference(LibraryPreferences::languageBadge)
        },
    )
    val showContinueViewingButton by screenModel.libraryPreferences.showContinueViewingButton().collectAsState()
    CheckboxItem(
        label = stringResource(R.string.action_display_show_continue_reading_button),
        checked = showContinueViewingButton,
        onClick = {
            screenModel.togglePreference(LibraryPreferences::showContinueViewingButton)
        },
    )

    HeadingItem(R.string.tabs_header)
    val categoryTabs by screenModel.libraryPreferences.categoryTabs().collectAsState()
    CheckboxItem(
        label = stringResource(R.string.action_display_show_tabs),
        checked = categoryTabs,
        onClick = {
            screenModel.togglePreference(LibraryPreferences::categoryTabs)
        },
    )
    val categoryNumberOfItems by screenModel.libraryPreferences.categoryNumberOfItems().collectAsState()
    CheckboxItem(
        label = stringResource(R.string.action_display_show_number_of_items),
        checked = categoryNumberOfItems,
        onClick = {
            screenModel.togglePreference(LibraryPreferences::categoryNumberOfItems)
        },
    )
}

// AM (GU) -->
data class GroupMode(
    val int: Int,
    val nameRes: Int,
    val drawableRes: Int,
)

private fun groupTypeDrawableRes(type: Int): Int {
    return when (type) {
        AnimeLibraryGroup.BY_STATUS -> R.drawable.ic_progress_clock_24dp
        AnimeLibraryGroup.BY_TRACK_STATUS -> R.drawable.ic_sync_24dp
        AnimeLibraryGroup.BY_SOURCE -> R.drawable.ic_browse_filled_24dp
        AnimeLibraryGroup.UNGROUPED -> R.drawable.ic_ungroup_24dp
        else -> R.drawable.ic_label_24dp
    }
}

@Composable
private fun ColumnScope.GroupPage(
    screenModel: AnimeLibrarySettingsScreenModel,
    hasCategories: Boolean,
) {
    val groups = remember(hasCategories, screenModel.trackServices) {
        buildList {
            add(AnimeLibraryGroup.BY_DEFAULT)
            add(AnimeLibraryGroup.BY_SOURCE)
            add(AnimeLibraryGroup.BY_STATUS)
            if (screenModel.trackServices.isNotEmpty()) {
                add(AnimeLibraryGroup.BY_TRACK_STATUS)
            }
            if (hasCategories) {
                add(AnimeLibraryGroup.UNGROUPED)
            }
        }.map {
            GroupMode(
                it,
                AnimeLibraryGroup.groupTypeStringRes(it, hasCategories),
                groupTypeDrawableRes(it),
            )
        }
    }

    groups.fastForEach {
        IconItem(
            label = stringResource(it.nameRes),
            icon = painterResource(it.drawableRes),
            selected = it.int == screenModel.grouping,
            onClick = {
                screenModel.setGrouping(it.int)
            },
        )
    }
}
// <-- AM (GU)
