package org.cstemp.artisanindex.programme

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.cstemp.artisanindex.artisan.Artisan
import java.util.*

@Entity
@Table(name = "programme")
class Programme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Size(min = 1, max = 70, message = "Title must be between 3 and 30 characters")
    @Column(name="title", nullable = false)
    var title: String? = null


    @Column(name = "batch", nullable = true)
    var batch: String = ""


    @Column(name = "year", nullable = true)
    var year: String = ""
    @Column(name = "description", nullable = true)
    var description: String? = null

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var created: Date = Date(System.currentTimeMillis())

    @ManyToMany
    @JoinTable(
        name = "artisan_programme",
        joinColumns = [JoinColumn(name = "programme_id")],
        inverseJoinColumns = [JoinColumn(name = "artisan_id")]
    )
    @JsonBackReference
    var artisans: MutableList<Artisan> = mutableListOf()

    fun addArtisan(artisan: Artisan) {
        artisans.add(artisan)
        artisan.programmes.add(this)
    }

    fun removeArtisan(artisan: Artisan) {
        artisans.remove(artisan)
        artisan.programmes.remove(this)
    }

}

