package com.mitron.connect.ui.screens.companies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mitron.connect.data.model.Company
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

@Composable
fun CompaniesTabContent(
    modifier: Modifier = Modifier,
    companies: List<Company>,
    onOpenCompany: (String) -> Unit
) {
    val colors = ConnectTheme.colors

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colors.surface1),
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        if (companies.isNotEmpty()) {
            items(companies) { company -> 
                CompanyRow(company = company, onClick = { onOpenCompany(company.id) })
                Divider(
                    color = colors.border,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 76.dp)
                )
            }
        }
    }
}

@Composable
private fun CompanyRow(company: Company, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    val (bg, fg) = company.color.tints(colors)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Business, contentDescription = null, tint = fg, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text(
                text = company.name, 
                style = MaterialTheme.typography.bodyLarge, 
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = company.descriptor ?: "Enterprise", 
                style = MaterialTheme.typography.bodyMedium, 
                color = colors.textSecondary
            )
        }
    }
}
