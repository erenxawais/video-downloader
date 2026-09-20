package com.example.videodownloader

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videodownloader.databinding.FragmentDownloadsBinding
import com.example.videodownloader.databinding.ItemDownloadBinding
import com.example.videodownloader.utils.FileUtils
import com.example.videodownloader.utils.PrefsManager
import java.io.File

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val list = PrefsManager.getHistory(requireContext())
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = DownloadsAdapter(list) { item ->
            val f = File(item.path)
            if (!f.exists()) {
                Toast.makeText(requireContext(), "File missing", Toast.LENGTH_SHORT).show()
                return@DownloadsAdapter
            }
            val uri = FileProvider.getUriForFile(
                requireContext(), requireContext().packageName + ".provider", f
            )
            val mime = if (item.type == "MP3") "audio/*" else "video/*"
            val i = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(i, "Open with"))
        }
        binding.empty.visibility    = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.recycler.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class DownloadsAdapter(
    private val items: List<PrefsManager.DownloadItem>,
    private val onClick: (PrefsManager.DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadsAdapter.VH>() {

    class VH(val b: ItemDownloadBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvName.text = item.title
        holder.b.tvMeta.text = "${item.type}  •  ${FileUtils.formatSize(item.size)}"
        holder.b.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}