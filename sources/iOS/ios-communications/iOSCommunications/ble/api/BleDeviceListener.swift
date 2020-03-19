
import RxSwift
import Foundation
import CoreBluetooth

/// BleState mapping from CB state
public enum BleState : Int {
    case unknown
    case resetting
    case unsupported
    case unauthorized
    case poweredOff
    case poweredOn
}

/// Ble central api
public protocol BleDeviceListener{
    
    /// helper to ask ble power state
    ///
    /// - Returns: true ble powered
    func blePowered() -> Bool
    
    /// ble power state
    ///
    /// - Returns: Observable ble power state
    func monitorBleState() -> Observable<BleState>
    
    /// enable or disable automatic reconnection
    var automaticReconnection: Bool {get set}

    /// enable or disable scan uuid filter
    var servicesToScanFor: [CBUUID]? {get set}
    
    /// enable or disable scan pre filter
    var scanPreFilter: ((_ content: BleAdvertisementContent) -> Bool)? {get set}
    
    /// enable or disable automatic H10 mapping
    var automaticH10Mapping: Bool {get set}
    
    /// set / get rssi limit for automatic reconnection
    var rssiLimitForConnection: Int32 {get set}
    
    /// Start scanning ble devices
    ///
    /// - Parameters:
    ///   - uuids: optional list of services, to look for from corebluetooth
    ///   - identifiers: device identifiers to look for from corebluetooth
    ///   - preFilter: pre filter before memory allocation, for pref reason
    /// - Returns: Observable stream of device advertisements
    func search(_ uuids: [CBUUID]?, identifiers: [UUID]?)  -> Observable<BleDeviceSession>
    
    /// Start connection request for device
    ///
    /// - Parameter session: session instance
    /// - Returns:
    func openSessionDirect(_ session: BleDeviceSession)
    
    /// all session state changes
    ///
    /// - Returns: Observable stream
    func monitorDeviceSessionState() -> Observable<(session: BleDeviceSession, state: BleDeviceSession.DeviceSessionState)>
    
    /// Start disconnection request for device
    ///
    /// - Parameter session: device to be disconnected
    /// - Returns:
    func closeSessionDirect(_ session: BleDeviceSession)
    
    /// request to clear all cached sessions
    ///
    /// - Parameter inState: set of states allowed to be removed default Closed | Park
    /// - Returns: count of session removed successfully
    @discardableResult
    func removeAllSessions(_ inState: Set<BleDeviceSession.DeviceSessionState>) -> Int
    @discardableResult
    func removeAllSessions() -> Int
    
    /// return all known sessions
    ///
    /// - Returns: list of sessions
    func allSessions() -> [BleDeviceSession]
}

public extension BleDeviceListener {
    func search(_ uuids: [CBUUID]? = nil, identifiers: [UUID]?=nil)  -> Observable<BleDeviceSession> {
        return search(uuids, identifiers: identifiers)
    }
}

/// extension to provide distinct 
public extension Observable where Element: Hashable {
    func distinct() -> Observable<Element> {
        var set = Set<Element>()
        return concatMap { element -> Observable<Element> in
            objc_sync_enter(self)
            defer {
                objc_sync_exit(self)
            }
            if set.contains(element) {
                return Observable<Element>.empty()
            } else {
                set.insert(element)
                return Observable<Element>.just(element)
            }
        }
    }
}