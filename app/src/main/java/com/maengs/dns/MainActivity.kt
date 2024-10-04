package com.maengs.dns

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.maengs.dns.databinding.ActivityMainBinding
import com.maengs.dns.ui.DnsResolverViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: DnsResolverViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.networkInterfaces.observe(this) {
            Log.d(TAG, "received network interfaces: $it")
            binding.networkInterfaces.apply {
                setSimpleItems(it.toTypedArray())
            }
        }

        binding.apply {
            actionQuery.setOnClickListener {
                val network = networkInterfaces.text ?: run {
                    return@setOnClickListener
                }
                val domain = domain.text ?: run {
                    return@setOnClickListener
                }

                viewModel.query("${network.trim()}", "${domain.trim()}")
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
