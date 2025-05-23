import VGSL

#if os(iOS)
import UIKit
#endif

/// The `TooltipActionPerformer` protocol defines actions for showing and hiding tooltips within the
/// DivKit.
///
/// Conforming to this protocol allows your class to perform actions related to tooltips. It
/// introduces two methods to control the display of tooltips:
/// - `showTooltip(info:)`: Use this method to show a tooltip with the provided `TooltipInfo`.
/// - `hideTooltip(id:)`: Use this method to hide the tooltip identified by the given `id`.
public protocol TooltipActionPerformer {
  /// Shows a tooltip with the provided `TooltipInfo`.
  ///
  /// - Parameter info: The `TooltipInfo` containing the necessary information to display the
  /// tooltip.
  func showTooltip(info: TooltipInfo)

  /// Hides the tooltip identified by the given `id`.
  ///
  /// - Parameter id: The identifier of the tooltip to hide.
  func hideTooltip(id: String)
}

#if os(iOS)
/// The `TooltipManager` protocol is a dependency responsible for processing and displaying tooltips
/// in the application.
///
/// Conforming to this protocol allows your class to act as a tooltip manager, handling various
/// tooltip-related tasks. It combines three other protocols as its superclasses:
/// - `TooltipActionPerformer`: Provides functionality for performing actions related to tooltips.
/// - `RenderingDelegate`: Acts as a delegate for rendering tooltips.
///
/// The `TooltipManager` protocol introduces two methods to handle tooltip anchor views:
/// - `tooltipAnchorViewAdded(anchorView:)`: Notifies the manager when a tooltip anchor view is
/// added to the view hieararchy.
/// - `tooltipAnchorViewRemoved(anchorView:)`: Notifies the manager when a tooltip anchor view is
/// removed from the view hieararchy.
public protocol TooltipManager: AnyObject, TooltipActionPerformer, RenderingDelegate {
  /// Notifies the manager when a tooltip anchor view is added to the view hieararchy.
  ///
  /// - Parameter anchorView: The event that anchorView appeared in view hierarchy.  An anchor view
  /// is the view to which a tooltip is attached.
  func tooltipAnchorViewAdded(anchorView: TooltipAnchorView)

  /// Notifies the manager when a tooltip anchor view is removed from the view hieararchy.
  ///
  /// - Parameter anchorView: The event that anchorView disappeared from view hierarchy.  An anchor
  /// view is the view to which a tooltip is attached.
  func tooltipAnchorViewRemoved(anchorView: TooltipAnchorView)

  /// Removes all tooltips.
  func reset()

  /// Sets handler for the tooltip view UI events.
  func setHandler(_ handler: @escaping (UIActionEvent) -> Void)
}

extension TooltipManager {
  public func setHandler(_: @escaping (UIActionEvent) -> Void) {}
}

public class DefaultTooltipManager: TooltipManager {
  public struct Tooltip {
    public let id: String
    public let duration: Duration
    public let view: VisibleBoundsTrackingView
  }

  public var shownTooltips: Property<Set<String>>

  private var handleAction: (UIActionEvent) -> Void
  private var existingAnchorViews = WeakCollection<TooltipAnchorView>()
  private var showingTooltips = [String: TooltipContainerView]()
  private(set) var tooltipWindow: UIWindow?
  private var previousOrientation = UIDevice.current.orientation

  public init(
    shownTooltips: Property<Set<String>> = Property(),
    handleAction: @escaping (UIActionEvent) -> Void = { _ in }
  ) {
    self.handleAction = handleAction
    self.shownTooltips = shownTooltips

    NotificationCenter.default.addObserver(
      self,
      selector: #selector(orientationDidChange),
      name: UIDevice.orientationDidChangeNotification,
      object: nil
    )
  }

  public func showTooltip(info: TooltipInfo) {
    setupTooltipWindow()

    guard let tooltipWindow,
          !showingTooltips.keys.contains(info.id)
    else { return }

    let windowBounds = tooltipWindow.bounds.inset(by: tooltipWindow.safeAreaInsets)

    Task { @MainActor in
      guard let tooltip = await existingAnchorViews.compactMap(
        concurrencyLimit: 1,
        transform: { await $0?.makeTooltip(id: info.id, in: windowBounds) }
      ).first else { return }
      let view = TooltipContainerView(
        tooltipView: tooltip.view,
        handleAction: handleAction,
        onCloseAction: { [weak self] in
          self?.showingTooltips.removeValue(forKey: tooltip.id)
          self?.tooltipWindow?.isHidden = true
        }
      )
      // Passing the statusBarStyle control to `rootViewController` of the main window
      let vc = ProxyViewController(
        viewController: UIApplication.shared.delegate?.window??
          .rootViewController ?? UIViewController()
      )
      vc.view = view
      // Window won't rotate if `rootViewController` is not set
      tooltipWindow.rootViewController = vc
      tooltipWindow.isHidden = false
      tooltipWindow.makeKeyAndVisible()
      view.frame = tooltipWindow.bounds
      showingTooltips[info.id] = view
      if !tooltip.duration.value.isZero {
        try await Task.sleep(nanoseconds: UInt64(tooltip.duration.value.nanoseconds))
        hideTooltip(id: tooltip.id)
      }
    }
  }

  public func hideTooltip(id: String) {
    guard let tooltipView = showingTooltips[id] else { return }
    tooltipView.close()
  }

  public func tooltipAnchorViewAdded(anchorView: TooltipAnchorView) {
    existingAnchorViews.append(anchorView)
  }

  public func tooltipAnchorViewRemoved(anchorView: TooltipAnchorView) {
    existingAnchorViews.remove(anchorView)
  }

  public func reset() {
    showingTooltips = [:]
    tooltipWindow = nil
  }

  public func setHandler(_ handler: @escaping (UIActionEvent) -> Void) {
    handleAction = handler
  }

  deinit {
    NotificationCenter.default.removeObserver(
      self,
      name: UIDevice.orientationDidChangeNotification,
      object: nil
    )
  }

  @objc func orientationDidChange(_: Notification) {
    let orientation = UIDevice.current.orientation
    guard orientation != previousOrientation, !orientation.isFlat else { return }
    if !(orientation.isPortrait && previousOrientation.isPortrait) {
      reset()
    }
    previousOrientation = orientation
  }

  private func setupTooltipWindow() {
    if tooltipWindow == nil {
      guard let windowScene = UIApplication.shared.connectedScenes
        .first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene
      else { return }
      tooltipWindow = UIWindow(windowScene: windowScene)
      tooltipWindow?.windowLevel = UIWindow.Level.alert + 1
      tooltipWindow?.isHidden = true
    }
  }
}

extension TooltipAnchorView {
  @MainActor
  fileprivate func makeTooltip(
    id: String,
    in constraint: CGRect
  ) async -> DefaultTooltipManager.Tooltip? {
    guard let tooltip = tooltips.first(where: { $0.id == id }) else {
      return nil
    }

    let tooltipView = await tooltip.tooltipViewFactory?() ?? tooltip.block.makeBlockView()

    let targetRect = window != nil ?
      convert(bounds, to: nil) :
      frame

    tooltipView.frame = tooltip.calculateFrame(
      targeting: targetRect,
      constrainedBy: constraint,
      useLegacyWidth: tooltip.useLegacyWidth
    )

    return DefaultTooltipManager.Tooltip(
      id: tooltip.id,
      duration: tooltip.duration,
      view: tooltipView
    )
  }
}

private final class ProxyViewController: UIViewController {
  private let viewController: UIViewController

  init(viewController: UIViewController) {
    self.viewController = viewController
    super.init(nibName: nil, bundle: nil)
  }

  @available(*, unavailable)
  required init?(coder _: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override var preferredStatusBarStyle: UIStatusBarStyle {
    viewController.preferredStatusBarStyle
  }
}
#else
public protocol TooltipManager: AnyObject, TooltipActionPerformer, RenderingDelegate {}

public final class DefaultTooltipManager: TooltipManager {
  public init() {}

  public func showTooltip(info _: TooltipInfo) {}
  public func hideTooltip(id _: String) {}
}
#endif

extension TooltipManager {
  public func mapView(_: BlockView, to _: BlockViewID) {}
  public func reset() {}
}
