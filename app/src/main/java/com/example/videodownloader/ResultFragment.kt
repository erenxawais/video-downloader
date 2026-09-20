package com.example.videodownloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.videodownloader.databinding.FragmentResultBinding
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private var videoUrl: String = ""

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DownloadService.ACTION_PROGRESS -> {
                    val p = intent.getFloatExtra("percent", 0f)
                    binding.progressBar.progress = p.toInt()
                    binding.tvStatus.text = "${p.toInt()}%"
                }
                DownloadService.ACTION_DONE -> {
                    setButtonsEnabled(true)
                    binding.tvStatus.text = "Done ✅"
                    Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                }
                DownloadService.ACTION_ERROR -> {
                    setButtonsEnabled(true)
                    binding.tvStatus.text = "Error ❌"
                    Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoUrl = arguments?.getString("video_url") ?: ""

        if (videoUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Video URL missing", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        loadVideoInfo()

        binding.btnMp4One.setOnClickListener { startDownload(Downloader.Type.MP4_360) }
        binding.btnMp4Two.setOnClickListener { startDownload(Downloader.Type.MP4_480) }
        binding.btnMp4Hd.setOnClickListener  { startDownload(Downloader.Type.MP4_HD) }
        binding.btnMp3.setOnClickListener    { startDownload(Downloader.Type.MP3) }
        binding.btnMore.setOnClickListener   { findNavController().popBackStack() }

        val filter = IntentFilter().apply {
            addAction(DownloadService.ACTION_PROGRESS)
            addAction(DownloadService.ACTION_DONE)
            addAction(DownloadService.ACTION_ERROR)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            requireContext().registerReceiver(progressReceiver, filter)
        }
    }

    private fun loadVideoInfo() {
        binding.pbThumb.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val info = Downloader.fetchInfo(videoUrl)
            binding.pbThumb.visibility = View.GONE
            if (info != null) {
                binding.tvTitle.text = info.title
                binding.tvDescription.text = info.description
                Glide.with(requireContext())
                    .load(info.thumbnailUrl)
                    .placeholder(android.R.color.darker_gray)
                    .into(binding.ivThumbnail)
            } else {
                binding.tvTitle.text = "Video info not available"
            }
        }
    }

    private fun startDownload(type: Downloader.Type) {
        setButtonsEnabled(false)
        binding.progressArea.visibility = View.VISIBLE
        binding.progressBar.progress = 0
        binding.tvStatus.text = "Starting…"

        val intent = Intent(requireContext(), DownloadService::class.java).apply {
            putExtra(DownloadService.EXTRA_URL, videoUrl)
            putExtra(DownloadService.EXTRA_TYPE, type.name)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnMp4One.isEnabled = enabled
        binding.btnMp4Two.isEnabled = enabled
        binding.btnMp4Hd.isEnabled  = enabled
        binding.btnMp3.isEnabled    = enabled
        binding.btnMore.isEnabled   = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        runCatching { requireContext().unregisterReceiver(progressReceiver) }
        _binding = null
    }
}