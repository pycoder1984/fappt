package com.vidking.firetv.settings

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.vidking.firetv.R
import com.vidking.firetv.data.AppPrefs

class SettingsFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance =
        GuidanceStylist.Guidance(
            getString(R.string.settings_title),
            getString(R.string.settings_description),
            getString(R.string.app_name),
            null
        )

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val ctx = requireContext()
        val baseUrl = AppPrefs.febboxBaseUrl(ctx)
        val token = AppPrefs.febboxToken(ctx)
        val embedFallback = AppPrefs.embedFallbackEnabled(ctx)
        val snifferFallback = AppPrefs.snifferFallbackEnabled(ctx)
        val livetvUrl = AppPrefs.livetvPlaylistUrl(ctx)

        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_LIVETV_URL)
                .title(getString(R.string.settings_livetv_url))
                .description(if (livetvUrl.isEmpty()) getString(R.string.settings_livetv_default) else livetvUrl)
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_BASE_URL)
                .title(getString(R.string.settings_febbox_base_url))
                .description(if (baseUrl.isEmpty()) getString(R.string.settings_not_set) else baseUrl)
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_TOKEN)
                .title(getString(R.string.settings_febbox_token))
                .description(maskToken(token))
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_SCRAPER_VIXSRC)
                .title(getString(R.string.settings_scraper_vixsrc))
                .description(toggleDescription(AppPrefs.scraperEnabled(ctx, "vixsrc")))
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_SCRAPER_VIDZEE)
                .title(getString(R.string.settings_scraper_vidzee))
                .description(toggleDescription(AppPrefs.scraperEnabled(ctx, "vidzee")))
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_SCRAPER_VIDSRC)
                .title(getString(R.string.settings_scraper_vidsrc))
                .description(toggleDescription(AppPrefs.scraperEnabled(ctx, "vidsrc")))
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_SNIFFER_FALLBACK)
                .title(getString(R.string.settings_sniffer_fallback))
                .description(
                    if (snifferFallback) getString(R.string.settings_sniffer_fallback_on)
                    else getString(R.string.settings_sniffer_fallback_off)
                )
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_EMBED_FALLBACK)
                .title(getString(R.string.settings_embed_fallback))
                .description(
                    if (embedFallback) getString(R.string.settings_embed_fallback_on)
                    else getString(R.string.settings_embed_fallback_off)
                )
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_CLEAR)
                .title(getString(R.string.settings_clear_febbox))
                .description(getString(R.string.settings_clear_febbox_desc))
                .build()
        )
        actions.add(
            GuidedAction.Builder(ctx)
                .id(ACTION_DONE)
                .title(getString(R.string.settings_done))
                .build()
        )
    }

    override fun onResume() {
        super.onResume()
        // Refresh descriptions in case a child step changed the underlying value.
        val ctx = requireContext()
        val baseUrl = AppPrefs.febboxBaseUrl(ctx)
        val token = AppPrefs.febboxToken(ctx)
        val embedFallback = AppPrefs.embedFallbackEnabled(ctx)
        val snifferFallback = AppPrefs.snifferFallbackEnabled(ctx)
        val livetvUrl = AppPrefs.livetvPlaylistUrl(ctx)
        findActionById(ACTION_LIVETV_URL)?.let {
            it.description = if (livetvUrl.isEmpty()) getString(R.string.settings_livetv_default) else livetvUrl
            notifyActionChanged(findActionPositionById(ACTION_LIVETV_URL))
        }
        findActionById(ACTION_BASE_URL)?.let {
            it.description = if (baseUrl.isEmpty()) getString(R.string.settings_not_set) else baseUrl
            notifyActionChanged(findActionPositionById(ACTION_BASE_URL))
        }
        findActionById(ACTION_TOKEN)?.let {
            it.description = maskToken(token)
            notifyActionChanged(findActionPositionById(ACTION_TOKEN))
        }
        findActionById(ACTION_SCRAPER_VIXSRC)?.let {
            it.description = toggleDescription(AppPrefs.scraperEnabled(ctx, "vixsrc"))
            notifyActionChanged(findActionPositionById(ACTION_SCRAPER_VIXSRC))
        }
        findActionById(ACTION_SCRAPER_VIDZEE)?.let {
            it.description = toggleDescription(AppPrefs.scraperEnabled(ctx, "vidzee"))
            notifyActionChanged(findActionPositionById(ACTION_SCRAPER_VIDZEE))
        }
        findActionById(ACTION_SCRAPER_VIDSRC)?.let {
            it.description = toggleDescription(AppPrefs.scraperEnabled(ctx, "vidsrc"))
            notifyActionChanged(findActionPositionById(ACTION_SCRAPER_VIDSRC))
        }
        findActionById(ACTION_SNIFFER_FALLBACK)?.let {
            it.description =
                if (snifferFallback) getString(R.string.settings_sniffer_fallback_on)
                else getString(R.string.settings_sniffer_fallback_off)
            notifyActionChanged(findActionPositionById(ACTION_SNIFFER_FALLBACK))
        }
        findActionById(ACTION_EMBED_FALLBACK)?.let {
            it.description =
                if (embedFallback) getString(R.string.settings_embed_fallback_on)
                else getString(R.string.settings_embed_fallback_off)
            notifyActionChanged(findActionPositionById(ACTION_EMBED_FALLBACK))
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_LIVETV_URL -> add(
                parentFragmentManager,
                TextEntryStepFragment.create(
                    field = TextEntryStepFragment.FIELD_LIVETV_URL,
                    title = getString(R.string.settings_livetv_url),
                    description = getString(R.string.settings_livetv_url_hint),
                    initialValue = AppPrefs.livetvPlaylistUrl(requireContext())
                )
            )
            ACTION_BASE_URL -> add(
                parentFragmentManager,
                TextEntryStepFragment.create(
                    field = TextEntryStepFragment.FIELD_BASE_URL,
                    title = getString(R.string.settings_febbox_base_url),
                    description = getString(R.string.settings_febbox_base_url_hint),
                    initialValue = AppPrefs.febboxBaseUrl(requireContext())
                )
            )
            ACTION_TOKEN -> add(
                parentFragmentManager,
                TextEntryStepFragment.create(
                    field = TextEntryStepFragment.FIELD_TOKEN,
                    title = getString(R.string.settings_febbox_token),
                    description = getString(R.string.settings_febbox_token_hint),
                    initialValue = AppPrefs.febboxToken(requireContext())
                )
            )
            ACTION_EMBED_FALLBACK -> {
                val ctx = requireContext()
                AppPrefs.setEmbedFallbackEnabled(ctx, !AppPrefs.embedFallbackEnabled(ctx))
                onResume()
            }
            ACTION_SNIFFER_FALLBACK -> {
                val ctx = requireContext()
                AppPrefs.setSnifferFallbackEnabled(ctx, !AppPrefs.snifferFallbackEnabled(ctx))
                onResume()
            }
            ACTION_SCRAPER_VIXSRC -> toggleScraper("vixsrc")
            ACTION_SCRAPER_VIDZEE -> toggleScraper("vidzee")
            ACTION_SCRAPER_VIDSRC -> toggleScraper("vidsrc")
            ACTION_CLEAR -> {
                val ctx = requireContext()
                AppPrefs.setFebboxBaseUrl(ctx, "")
                AppPrefs.setFebboxToken(ctx, "")
                onResume()
            }
            ACTION_DONE -> requireActivity().finish()
        }
    }

    private fun maskToken(token: String): String {
        if (token.isEmpty()) return getString(R.string.settings_not_set)
        if (token.length <= 6) return "•".repeat(token.length)
        return token.substring(0, 3) + "•".repeat(token.length - 6) + token.substring(token.length - 3)
    }

    private fun toggleDescription(enabled: Boolean): String =
        if (enabled) getString(R.string.settings_toggle_on) else getString(R.string.settings_toggle_off)

    private fun toggleScraper(id: String) {
        val ctx = requireContext()
        AppPrefs.setScraperEnabled(ctx, id, !AppPrefs.scraperEnabled(ctx, id))
        onResume()
    }

    companion object {
        private const val ACTION_LIVETV_URL = 0L
        private const val ACTION_BASE_URL = 1L
        private const val ACTION_TOKEN = 2L
        private const val ACTION_EMBED_FALLBACK = 3L
        private const val ACTION_CLEAR = 4L
        private const val ACTION_DONE = 5L
        private const val ACTION_SNIFFER_FALLBACK = 6L
        private const val ACTION_SCRAPER_VIXSRC = 7L
        private const val ACTION_SCRAPER_VIDZEE = 8L
        private const val ACTION_SCRAPER_VIDSRC = 9L
    }
}
