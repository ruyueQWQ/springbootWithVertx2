using UnityEngine;
using UnityEngine.UI;

/// <summary>
/// 窗口控制器 - 用于控制游戏窗口的大小
/// </summary>
public class WindowController : MonoBehaviour
{
    // 缩小后的窗口分辨率
    [Header("窗口设置")]
    public int smallWindowWidth = 800;
    public int smallWindowHeight = 600;
    
    // 原始窗口分辨率
    private int originalWidth;
    private int originalHeight;
    private bool isFullScreen;
    
    // UI按钮引用（可选）
    [Header("UI引用")]
    public Button shrinkWindowButton;
    public Button restoreWindowButton;
    
    private void Start()
    {
        // 保存原始窗口设置
        originalWidth = Screen.width;
        originalHeight = Screen.height;
        isFullScreen = Screen.fullScreen;
        
        // 如果提供了按钮引用，则添加点击事件监听
        if (shrinkWindowButton != null)
        {
            shrinkWindowButton.onClick.AddListener(ShrinkWindow);
        }
        
        if (restoreWindowButton != null)
        {
            restoreWindowButton.onClick.AddListener(RestoreWindow);
        }
        
        Debug.Log("WindowController initialized. Original resolution: " + originalWidth + "x" + originalHeight);
    }
    
    private void Update()
    {
        // 添加快捷键支持
        // F11: 缩小窗口
        if (Input.GetKeyDown(KeyCode.F11))
        {
            ShrinkWindow();
        }
        
        // F12: 恢复原始窗口
        if (Input.GetKeyDown(KeyCode.F12))
        {
            RestoreWindow();
        }
    }
    
    /// <summary>
    /// 缩小窗口到指定分辨率
    /// </summary>
    public void ShrinkWindow()
    {
        Screen.SetResolution(smallWindowWidth, smallWindowHeight, false);
        Debug.Log("Window shrunk to: " + smallWindowWidth + "x" + smallWindowHeight);
    }
    
    /// <summary>
    /// 恢复窗口到原始分辨率
    /// </summary>
    public void RestoreWindow()
    {
        Screen.SetResolution(originalWidth, originalHeight, isFullScreen);
        Debug.Log("Window restored to original size: " + originalWidth + "x" + originalHeight);
    }
    
    /// <summary>
    /// 设置自定义窗口大小
    /// </summary>
    /// <param name="width">窗口宽度</param>
    /// <param name="height">窗口高度</param>
    public void SetCustomWindowSize(int width, int height)
    {
        Screen.SetResolution(width, height, false);
        Debug.Log("Window size set to: " + width + "x" + height);
    }
}