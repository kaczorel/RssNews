package pl.edu.pja.s27599.rssnews.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "rss")
@JsonIgnoreProperties(ignoreUnknown = true)
data class RssFeed(
    @JacksonXmlProperty(localName = "channel")
    val channel: Channel? = null
)

@JacksonXmlRootElement(localName = "channel")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Channel(
    @JacksonXmlProperty(localName = "title")
    val title: String? = null,
    @JacksonXmlProperty(localName = "link")
    val link: String? = null,
    @JacksonXmlProperty(localName = "description")
    val description: String? = null,

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val item: List<Item>? = null
)

@JacksonXmlRootElement(localName = "item")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Item(
    @JacksonXmlProperty(localName = "title")
    val title: String? = null,
    @JacksonXmlProperty(localName = "link")
    val link: String? = null,
    @JacksonXmlProperty(localName = "description")
    val description: String? = null,
    @JacksonXmlProperty(localName = "pubDate")
    val pubDate: String? = null,
    @JacksonXmlProperty(localName = "guid")
    val guid: String? = null,

    @JacksonXmlProperty(localName = "enclosure")
    val enclosure: Enclosure? = null
)

@JacksonXmlRootElement(localName = "enclosure")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Enclosure(
    @JacksonXmlProperty(isAttribute = true, localName = "url")
    val url: String? = null,
    @JacksonXmlProperty(isAttribute = true, localName = "type")
    val type: String? = null,
    @JacksonXmlProperty(isAttribute = true, localName = "length")
    val length: String? = null
)