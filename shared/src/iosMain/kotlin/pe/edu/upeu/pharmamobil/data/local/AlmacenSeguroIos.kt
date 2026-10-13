@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package pe.edu.upeu.pharmamobil.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

class AlmacenSeguroIos : AlmacenSeguro {

    override fun guardarToken(token: String) {
        borrarToken()
        val datos = NSString.create(string = token)
            .dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val consulta = consultaBase()
        CFDictionaryAddValue(consulta, kSecValueData, CFBridgingRetain(datos))
        SecItemAdd(consulta, null)
        CFRelease(consulta)
    }

    override fun leerToken(): String? = memScoped {
        val consulta = consultaBase()
        CFDictionaryAddValue(consulta, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(consulta, kSecMatchLimit, kSecMatchLimitOne)
        val resultado = alloc<CFTypeRefVar>()
        val estado = SecItemCopyMatching(consulta, resultado.ptr)
        CFRelease(consulta)
        if (estado != errSecSuccess) return@memScoped null
        val datos = CFBridgingRelease(resultado.value) as? NSData
        datos?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString() }
    }

    override fun borrarToken() {
        val consulta = consultaBase()
        SecItemDelete(consulta)
        CFRelease(consulta)
    }

    private fun consultaBase(): CFMutableDictionaryRef? {
        val d = CFDictionaryCreateMutable(
            null, 0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionaryAddValue(d, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(d, kSecAttrService, CFBridgingRetain("pharmamobil"))
        CFDictionaryAddValue(d, kSecAttrAccount, CFBridgingRetain("jwt"))
        return d
    }
}

