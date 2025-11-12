using UnityEngine;
using System.Collections.Generic;
using System;

/// <summary>
/// 用于在Unity中将操作从后台线程调度到主线程执行
/// </summary>
public class MainThreadDispatcher : MonoBehaviour
{
    private static readonly Queue<Action> _executionQueue = new Queue<Action>();
    private static MainThreadDispatcher _instance;
    
    public static MainThreadDispatcher Instance()
    {
        if (_instance == null)
        {
            GameObject obj = new GameObject("MainThreadDispatcher");
            _instance = obj.AddComponent<MainThreadDispatcher>();
            DontDestroyOnLoad(obj);
        }
        return _instance;
    }
    
    public void Enqueue(Action action)
    {
        lock (_executionQueue)
        {
            _executionQueue.Enqueue(action);
        }
    }
    
    void Update()
    {
        lock (_executionQueue)
        {
            while (_executionQueue.Count > 0)
            {
                _executionQueue.Dequeue().Invoke();
            }
        }
    }
}