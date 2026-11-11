package app.daopapo.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PapoApiTest {
    @Test fun `aceita o endereco de varios jeitos`() {
        val certo = "https://da-o-papo.onrender.com"
        assertEquals(certo, PapoApi.enderecoBase("https://da-o-papo.onrender.com"))
        assertEquals(certo, PapoApi.enderecoBase("https://da-o-papo.onrender.com/"))
        assertEquals(certo, PapoApi.enderecoBase(" https://da-o-papo.onrender.com/api/saude "))
        assertEquals(certo, PapoApi.enderecoBase("https://da-o-papo.onrender.com/api/perguntar/"))
        assertEquals(certo, PapoApi.enderecoBase("https://da-o-papo.onrender.com/api"))
    }
}
