using UnityEngine;
using System;
using System.Net.Sockets;
using System.Threading;
using System.Text;
using Google.Protobuf;
using GameClient.Protobuf;

public class UnityClientExample : MonoBehaviour
{
    private TcpClient tcpClient;
    private NetworkStream stream;
    private Thread receiveThread;
    private bool isConnected = false;
    private string serverIp = "127.0.0.1";
    private int serverPort = 9000;
    
    // 游戏状态
    private long playerId;
    private long roomId;
    private string roomCode;
    private bool isGameStarted = false;
    private Vector3 playerPosition = new Vector3(0, 0, 0);
    private Vector3 opponentPosition = new Vector3(0, 0, 0);
    
    // UI引用
    public UnityEngine.UI.Text statusText;
    public UnityEngine.UI.InputField usernameInput;
    public UnityEngine.UI.InputField passwordInput;
    public UnityEngine.UI.InputField nicknameInput;
    public UnityEngine.UI.InputField roomCodeInput;
    
    void Start()
    {
        // 初始化连接
        ConnectToServer();
    }
    
    void Update()
    {
        // 处理键盘输入，控制方块移动
        
        
         // 使用isConnected替代isGameStarted，这样连接后就能测试移动
        {
            float horizontal = Input.GetAxis("Horizontal");
            float vertical = Input.GetAxis("Vertical");
            
            Vector3 newPosition = playerPosition + new Vector3(horizontal * Time.deltaTime * 5f, 0, vertical * Time.deltaTime * 5f);
            
            // 限制在游戏区域内
            newPosition.x = Mathf.Clamp(newPosition.x, -10f, 10f);
            newPosition.z = Mathf.Clamp(newPosition.z, -10f, 10f);
            
            if (newPosition != playerPosition)
            {
                playerPosition = newPosition;
                SendMoveRequest(playerPosition.x, playerPosition.z);
            }
        }
    }
    
    void OnDestroy()
    {
        Disconnect();
    }
    
    // 连接到服务器
    public void ConnectToServer()
    {
        try
        {
            tcpClient = new TcpClient(serverIp, serverPort);
            stream = tcpClient.GetStream();
            isConnected = true;
            
            // 启动接收线程
            receiveThread = new Thread(ReceiveMessages);
            receiveThread.IsBackground = true;
            receiveThread.Start();
            
            UpdateStatus("已连接到服务器");
        }
        catch (Exception e)
        {
            UpdateStatus("连接服务器失败: " + e.Message);
            Debug.LogError("连接服务器失败: " + e.Message);
        }
    }
    
    // 断开连接
    public void Disconnect()
    {
        isConnected = false;
        
        if (receiveThread != null && receiveThread.IsAlive)
        {
            receiveThread.Abort();
        }
        
        if (stream != null)
        {
            stream.Close();
        }
        
        if (tcpClient != null)
        {
            tcpClient.Close();
        }
        
        UpdateStatus("已断开连接");
    }
    
    // 接收消息
    private void ReceiveMessages()
    {
        byte[] buffer = new byte[4096];
        
        while (isConnected)
        {
            try
            {
                if (stream.DataAvailable)
                {
                    int bytesRead = stream.Read(buffer, 0, buffer.Length);
                    if (bytesRead > 0)
                    {
                        byte[] messageBytes = new byte[bytesRead];
                        Array.Copy(buffer, messageBytes, bytesRead);
                        
                        // 解析消息
                        ParseMessage(messageBytes);
                    }
                }
                
                Thread.Sleep(10);
            }
            catch (Exception e)
            {
                Debug.LogError("接收消息出错: " + e.Message);
                isConnected = false;
            }
        }
    }
    
    // 发送消息
    private void SendMessage(GameMessage message)
    {
        debug.Log("发送消息: " + message.Type);
        if (isConnected)
        {
            try
            {
                byte[] bytes = message.ToByteArray();
                stream.Write(bytes, 0, bytes.Length);
            }
            catch (Exception e)
            {
                Debug.LogError("发送消息出错: " + e.Message);
            }
        }
    }
    
    // 解析消息
    private void ParseMessage(byte[] messageBytes)
    {
        try
        {
            GameMessage message = GameMessage.Parser.ParseFrom(messageBytes);
            
            switch (message.Type)
            {
                case MessageType.LoginResponse:
                    HandleLoginResponse(message.LoginResponse);
                    break;
                case MessageType.RegisterResponse:
                    HandleRegisterResponse(message.RegisterResponse);
                    break;
                case MessageType.CreateRoomResponse:
                    HandleCreateRoomResponse(message.CreateRoomResponse);
                    break;
                case MessageType.JoinRoomResponse:
                    HandleJoinRoomResponse(message.JoinRoomResponse);
                    break;
                case MessageType.ListRoomsResponse:
                    HandleListRoomsResponse(message.ListRoomsResponse);
                    break;
                case MessageType.StartGameResponse:
                    HandleStartGameResponse(message.StartGameResponse);
                    break;
                case MessageType.GameStateUpdate:
                    HandleGameStateUpdate(message.GameStateUpdate);
                    break;
                case MessageType.GameOver:
                    HandleGameOver(message.GameOver);
                    break;
                case MessageType.Error:
                    HandleError(message.Error);
                    break;
            }
        }
        catch (Exception e)
        {
            Debug.LogError("解析消息出错: " + e.Message);
        }
    }
    
    // UI按钮事件处理
    public void OnLoginButtonClick()
    {
        string username = usernameInput.text;
        string password = passwordInput.text;
        
        if (!string.IsNullOrEmpty(username) && !string.IsNullOrEmpty(password))
        {
            SendLoginRequest(username, password);
        }
    }
    
    public void OnRegisterButtonClick()
    {
        string username = usernameInput.text;
        string password = passwordInput.text;
        string nickname = nicknameInput.text;
        
        if (!string.IsNullOrEmpty(username) && !string.IsNullOrEmpty(password) && !string.IsNullOrEmpty(nickname))
        {
            SendRegisterRequest(username, password, nickname);
        }
    }
    
    public void OnCreateRoomButtonClick()
    {
        if (playerId > 0)
        {
            SendCreateRoomRequest();
        }
    }
    
    public void OnJoinRoomButtonClick()
    {
        string code = roomCodeInput.text;
        if (playerId > 0 && !string.IsNullOrEmpty(code))
        {
            SendJoinRoomRequest(code);
        }
    }
    
    public void OnStartGameButtonClick()
    {
        if (playerId > 0 && roomId > 0)
        {
            SendStartGameRequest();
        }
    }
    
    // 发送各种请求
    private void SendLoginRequest(string username, string password)
    {
        var request = new LoginRequest {
            Username = username,
            Password = password
        };
        
        var message = new GameMessage {
            Type = MessageType.LoginRequest,
            LoginRequest = request
        };
        
        SendMessage(message);
    }
    
    private void SendRegisterRequest(string username, string password, string nickname)
    {
        var request = new RegisterRequest {
            Username = username,
            Password = password,
            Nickname = nickname
        };
        
        var message = new GameMessage {
            Type = MessageType.RegisterRequest,
            RegisterRequest = request
        };
        
        SendMessage(message);
    }
    
    private void SendCreateRoomRequest()
    {
        var request = new CreateRoomRequest {
            PlayerId = playerId
        };
        
        var message = new GameMessage {
            Type = MessageType.CreateRoomRequest,
            CreateRoomRequest = request
        };
        
        SendMessage(message);
    }
    
    private void SendJoinRoomRequest(string roomCode)
    {
        var request = new JoinRoomRequest {
            PlayerId = playerId,
            RoomCode = roomCode
        };
        
        var message = new GameMessage {
            Type = MessageType.JoinRoomRequest,
            JoinRoomRequest = request
        };
        
        SendMessage(message);
    }
    
    private void SendStartGameRequest()
    {
        var request = new StartGameRequest {
            RoomId = roomId,
            PlayerId = playerId
        };
        
        var message = new GameMessage {
            Type = MessageType.StartGameRequest,
            StartGameRequest = request
        };
        
        SendMessage(message);
    }
    
    private void SendMoveRequest(float x, float y)
    {
        var request = new MoveRequest {
            RoomId = roomId,
            PlayerId = playerId,
            X = x,
            Y = y
        };
        
        var message = new GameMessage {
            Type = MessageType.MoveRequest,
            MoveRequest = request
        };
        
        SendMessage(message);
    }
    
    // 处理各种响应
    private void HandleLoginResponse(LoginResponse response)
    {
        if (response.Code == ErrorCode.Success)
        {
            playerId = response.PlayerInfo.Id;
            UpdateStatus("登录成功: " + response.PlayerInfo.Nickname);
        }
        else
        {
            UpdateStatus("登录失败: " + response.Message);
        }
    }
    
    private void HandleRegisterResponse(RegisterResponse response)
    {
        if (response.Code == ErrorCode.Success)
        {
            playerId = response.PlayerInfo.Id;
            UpdateStatus("注册成功: " + response.PlayerInfo.Nickname);
        }
        else
        {
            UpdateStatus("注册失败: " + response.Message);
        }
    }
    
    private void HandleCreateRoomResponse(CreateRoomResponse response)
    {
        if (response.Code == ErrorCode.Success)
        {
            roomId = response.RoomInfo.Id;
            roomCode = response.RoomInfo.RoomCode;
            UpdateStatus("房间创建成功，房间码: " + roomCode);
        }
        else
        {
            UpdateStatus("房间创建失败: " + response.Message);
        }
    }
    
    private void HandleJoinRoomResponse(JoinRoomResponse response)
    {
        if (response.Code == ErrorCode.Success)
        {
            roomId = response.RoomInfo.Id;
            roomCode = response.RoomInfo.RoomCode;
            UpdateStatus("加入房间成功");
        }
        else
        {
            UpdateStatus("加入房间失败: " + response.Message);
        }
    }
    
    private void HandleListRoomsResponse(ListRoomsResponse response)
    {
        if (response.Code == ErrorCode.Success)
        {
            UpdateStatus("获取房间列表成功，共 " + response.Rooms.Count + " 个房间");
            // 这里可以更新UI显示房间列表
        }
        else
        {
            UpdateStatus("获取房间列表失败: " + response.Message);
        }
    }
    
    private void HandleStartGameResponse(StartGameResponse response)
    {
        if (response.Code == ErrorCode.Success)
        {
            isGameStarted = true;
            UpdateStatus("游戏开始！");
            // 初始化玩家位置
            playerPosition = new Vector3(0, 0, 0);
            opponentPosition = new Vector3(0, 0, 0);
        }
        else
        {
            UpdateStatus("游戏开始失败: " + response.Message);
        }
    }
    
    private void HandleGameStateUpdate(GameStateUpdate update)
    {
        foreach (var playerPos in update.Players)
        {
            if (playerPos.PlayerId == playerId)
            {
                // 更新自己的位置（如果需要）
                playerPosition = new Vector3(playerPos.X, 0, playerPos.Y);
            }
            else
            {
                // 更新对手的位置
                opponentPosition = new Vector3(playerPos.X, 0, playerPos.Y);
            }
        }
    }
    
    private void HandleGameOver(GameOver gameOver)
    {
        isGameStarted = false;
        if (gameOver.WinnerId == playerId)
        {
            UpdateStatus("游戏结束，你赢了！");
        }
        else
        {
            UpdateStatus("游戏结束，对手赢了！");
        }
    }
    
    private void HandleError(ErrorMessage error)
    {
        UpdateStatus("错误: " + error.Message);
    }
    
    // 更新状态文本
    private void UpdateStatus(string status)
    {
        Debug.Log(status);
        
        // 在主线程更新UI
        UnityMainThreadDispatcher.Instance().Enqueue(() =>
        {
            if (statusText != null)
            {
                statusText.text = status;
            }
        });
    }
}