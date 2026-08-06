package com.piiagent.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piiagent.app.R
import com.piiagent.app.adapter.RecentActivityAdapter
import com.piiagent.app.databinding.FragmentDashboardBinding
import com.piiagent.app.model.RecentActivityItem

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindDummyData()
        setupRecentActivity()
        setupQuickActions()
    }

    private fun bindDummyData() {
        binding.txtWelcome.text = getString(R.string.dashboard_welcome_prefix)
        binding.txtUserName.text = getString(R.string.dashboard_user_name)
        binding.txtPrivacyScoreValue.text = "78"
        binding.txtRiskLevelValue.text = getString(R.string.value_risk_level)
        binding.txtBrokersFound.text = "23"
        binding.txtActiveRequests.text = "6"
        binding.txtCompletedRequests.text = "14"
        binding.txtPendingRequests.text = "3"
    }

    private fun setupRecentActivity() {
        val dummyActivity = listOf(
            RecentActivityItem(
                "Data broker scan completed",
                "14 new exposures found across 6 brokers",
                "2h ago",
                R.drawable.ic_nav_scan
            ),
            RecentActivityItem(
                "Removal request sent",
                "Request submitted to Spokeo",
                "5h ago",
                R.drawable.ic_nav_requests
            ),
            RecentActivityItem(
                "Broker confirmed deletion",
                "WhitePages confirmed data removal",
                "1d ago",
                R.drawable.ic_stat_completed
            ),
            RecentActivityItem(
                "New exposure detected",
                "Your data found on a new broker site",
                "2d ago",
                R.drawable.ic_risk_warning
            )
        )

        binding.recyclerRecentActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RecentActivityAdapter(dummyActivity)
        }
    }

    private fun setupQuickActions() {
        val navController = findNavController()

        binding.cardStartScan.setOnClickListener {
            navController.navigate(R.id.nav_scan)
        }
        binding.cardViewRequests.setOnClickListener {
            navController.navigate(R.id.nav_requests)
        }
        binding.cardNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "No new notifications", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
