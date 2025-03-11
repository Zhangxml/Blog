```java
关于input的debug流程

WindowManagerService.java#updateFocusedWindowLocked()
    mRoot.updateFocusedWindowLocked(mode, updateInputWindows)
RootWindowContainer.java#updateFocusedWindowLocked()
    dc.updateFocusedWindowLocked(mode, updateInputWindows, topFocusedDisplayId)
DisplayContent.iava#updateFocusedWindowLocked()
    WindowState newFocus = findFocusedWindowIfNeeded(topFocusedDisplayId)
    DisplayContent.iava#findFocusedWindow()
    DisplayContent.iava#mFindFocusedWindow = w ->{}
    
```

