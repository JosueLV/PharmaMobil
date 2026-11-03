import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        configurarFirebaseSiExiste()
        KoinIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}