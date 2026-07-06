package com.mitron.connect.ui.screens.companies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mitron.connect.data.SampleData
import com.mitron.connect.data.model.Company
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

@Composable
fun CompaniesTabContent(
    modifier: Modifier = Modifier,
    companies: List<Company>,
    onOpenCompany: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        items(companies) { company -> CompanyRow(company, onClick = { onOpenCompany(company.id) }) }
    }
}

@Composable
private fun CompanyRow(company: Company, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    val (bg, fg) = company.color.tints(colors)
    ConnectCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(bg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Business, contentDescription = null, tint = fg)
            }
            Column(modifier = Modifier.padding(start = Spacing.xs)) {
                Text(company.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(company.descriptor ?: "", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun CompaniesTabContentPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        CompaniesTabContent(companies = SampleData.companies, onOpenCompany = {})
    }
}
