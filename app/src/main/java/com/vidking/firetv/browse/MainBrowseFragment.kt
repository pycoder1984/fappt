package com.vidking.firetv.browse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.lifecycleScope
import com.vidking.firetv.R
import com.vidking.firetv.details.DetailsActivity
import com.vidking.firetv.presenters.CardPresenter
import com.vidking.firetv.search.SearchActivity
import com.vidking.firetv.settings.SettingsActivity
import com.vidking.firetv.tmdb.MediaItem
import com.vidking.firetv.tmdb.Tmdb
import kotlinx.coroutines.launch

class MainBrowseFragment : BrowseSupportFragment() {
    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.browse_title)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireContext(), R.color.brand_red)
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.brand_red)

        setOnSearchClickedListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is MediaItem -> {
                    val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
                        putExtra(DetailsActivity.EXTRA_TMDB_ID, item.id)
                        putExtra(DetailsActivity.EXTRA_MEDIA_TYPE, if (item.isMovie()) "movie" else "tv")
                    }
                    startActivity(intent)
                }
                is SettingsCardItem -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }
            }
        }

        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
        loadRows()
    }

    private fun loadRows() {
        val cardPresenter = CardPresenter()
        val trendingAdapter = ArrayObjectAdapter(cardPresenter)
        val moviesAdapter = ArrayObjectAdapter(cardPresenter)
        val tvAdapter = ArrayObjectAdapter(cardPresenter)
        val settingsAdapter = ArrayObjectAdapter(cardPresenter)

        rowsAdapter.add(ListRow(HeaderItem(1, getString(R.string.header_trending)), trendingAdapter))
        rowsAdapter.add(ListRow(HeaderItem(2, getString(R.string.header_popular_movies)), moviesAdapter))
        rowsAdapter.add(ListRow(HeaderItem(3, getString(R.string.header_popular_tv)), tvAdapter))

        settingsAdapter.add(
            SettingsCardItem(
                title = getString(R.string.settings_title),
                subtitle = getString(R.string.settings_card_subtitle)
            )
        )
        rowsAdapter.add(ListRow(HeaderItem(4, getString(R.string.header_settings)), settingsAdapter))

        loadInto("trending", trendingAdapter) { Tmdb.api.trending(Tmdb.API_KEY).results }
        loadInto("popular movies", moviesAdapter) { Tmdb.api.popularMovies(Tmdb.API_KEY).results }
        loadInto("popular tv", tvAdapter) { Tmdb.api.popularTv(Tmdb.API_KEY).results }
    }

    private fun loadInto(
        label: String,
        adapter: ArrayObjectAdapter,
        block: suspend () -> List<MediaItem>
    ) {
        lifecycleScope.launch {
            try {
                val items = block()
                items.forEach { adapter.add(it) }
            } catch (t: Throwable) {
                Log.e("Vidking", "Failed to load $label", t)
                Toast.makeText(
                    requireContext(),
                    "Failed to load $label: ${t.javaClass.simpleName} ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
