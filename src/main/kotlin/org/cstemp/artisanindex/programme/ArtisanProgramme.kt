package org.cstemp.artisanindex.programme

import jakarta.persistence.*
import org.cstemp.artisanindex.artisan.Artisan

@Entity
@Table(name = "artisan_programme")
class ArtisanProgramme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "artisan_id", referencedColumnName = "id")
    var artisan: Artisan? = null

    @ManyToOne
    @JoinColumn(name = "programme_id", referencedColumnName = "id")
    var programme: Programme? = null
}