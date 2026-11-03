import FirebaseCore
import Foundation

// El SDK nativo también queda aislado en la capa de datos de la aplicación iOS.
func configurarFirebaseSiExiste() {
    if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil,
       FirebaseApp.app() == nil {
        FirebaseApp.configure()
    }
}
