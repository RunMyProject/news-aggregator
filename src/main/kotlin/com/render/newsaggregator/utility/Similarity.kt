// This package contains utility classes for news aggregation and analysis
package com.render.newsaggregator.utility

// Jaro object contains functions for computing string similarity using Jaro algorithm
object Jaro {

    /**
     * Computes the Jaro similarity score between two input strings.
     *
     * @param s1 the first input string
     * @param s2 the second input string
     * @return the Jaro similarity score between s1 and s2 as a double value
     */
    fun similarity(s1: String, s2: String): Double {

        // Implementation of the Jaro algorithm
        //
        val s1_len = s1.length
        val s2_len = s2.length
        if (s1_len == 0 && s2_len == 0) return 1.0
        val match_distance = Math.max(s1_len, s2_len) / 2 - 1
        val s1_matches = BooleanArray(s1_len)
        val s2_matches = BooleanArray(s2_len)
        var matches = 0
        for (i in 0..s1_len - 1) {
            val start = Math.max(0, i - match_distance)
            val end = Math.min(i + match_distance + 1, s2_len)
            (start..end - 1).find { j -> !s2_matches[j] && s1[i] == s2[j] } ?. let {
                s1_matches[i] = true
                s2_matches[it] = true
                matches++
            }
        }
        if (matches == 0) return 0.0
        var t = 0.0
        var k = 0
        (0..s1_len - 1).filter { s1_matches[it] }.forEach { i ->
            while (!s2_matches[k]) k++
            if (s1[i] != s2[k]) t += 0.5
            k++
        }

        val m = matches.toDouble()
        return (m / s1_len + m / s2_len + (m - t) / m) / 3.0
    }
}
