package com.example.mimshak_final_afekoin

import io.supabase.common.SupabaseUrl
import io.supabase.createSupabaseClient
import io.supabase.gotrue.GoTrue
import io.supabase.postgrest.Postgrest
import io.supabase.realtime.Realtime

object SupabaseManager {

    private const val SUPABASE_URL = "https://your-project-id.supabase.co" // IMPORTANT: Replace with your actual Supabase URL
    private const val SUPABASE_KEY = "your-anon-key" // IMPORTANT: Replace with your actual Supabase anon key

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Realtime)
    }
}