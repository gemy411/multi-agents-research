package com.gemy.agents.search.sonar

/**
 * Sonar doesn't web scrape anyways, but it can fetch digest from the page
 * @property DirectHit ask sonar to get the page synthesized digest directly
 * @property SectionBased ask sonar to split the page into x sections and then query sonar again to write each section
 * on its own, then merge all
 */
sealed class SonarWebFetchMode {
    data object DirectHit: SonarWebFetchMode()
    data object SectionBased: SonarWebFetchMode()
}