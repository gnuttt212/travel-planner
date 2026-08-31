import { useEffect, useState } from 'react';
import { getAuthTokenQueryParam } from '../api';
import type { FriendRequestDto } from '../api';
import { 
  useProfile, 
  useFriends, 
  useIncomingRequests, 
  useOutgoingRequests, 
  useSearchUsers, 
  useUpdateProfile,
  useSendFriendRequest,
  useRespondFriendRequest,
  useRemoveFriend
} from '../hooks/useUserProfile';
import { useConversation, useSendMessage } from '../hooks/useMessaging';
import { useQueryClient } from '@tanstack/react-query';

const DEFAULT_AVATAR = 'https://api.dicebear.com/6.x/identicon/svg?seed=travel-planner';

export default function Profile() {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [editMode, setEditMode] = useState(false);
  const [activeTab, setActiveTab] = useState<'profile' | 'friends' | 'requests' | 'search'>('profile');
  const [notification, setNotification] = useState<string>('');
  
  // Chat state
  const [chatWith, setChatWith] = useState<string | null>(null);
  const [chatInput, setChatInput] = useState('');
  
  // Profile edit state
  const [displayName, setDisplayName] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [bio, setBio] = useState('');
  
  const [errorMessage, setErrorMessage] = useState('');

  // Queries
  const { data: profile, isLoading: isProfileLoading, isError: isProfileError } = useProfile();
  const { data: friends = [] } = useFriends();
  const { data: incoming = [] } = useIncomingRequests();
  const { data: outgoing = [] } = useOutgoingRequests();
  const { data: searchResults = [] } = useSearchUsers(query);
  const { data: chatMessages = [] } = useConversation(chatWith);

  // Mutations
  const updateProfileMutation = useUpdateProfile();
  const sendRequestMutation = useSendFriendRequest();
  const respondRequestMutation = useRespondFriendRequest();
  const removeFriendMutation = useRemoveFriend();
  const sendMessageMutation = useSendMessage();

  // Initialize edit state when profile is loaded or edit mode toggles
  useEffect(() => {
    if (profile && editMode) {
      setDisplayName(profile.displayName || '');
      setAvatarUrl(profile.avatarUrl || '');
      setBio(profile.bio || '');
    }
  }, [profile, editMode]);

  // EventSource for real-time notifications
  useEffect(() => {
    const eventSource = new EventSource(`/api/v1/profile/friend-requests/stream${getAuthTokenQueryParam()}`);
    eventSource.addEventListener('friend-request', (event) => {
      const data = JSON.parse((event as MessageEvent).data) as FriendRequestDto;
      setNotification(`Bạn có lời mời mới từ ${data.senderEmail}`);
      queryClient.invalidateQueries({ queryKey: ['friend-requests', 'incoming'] });
    });
    eventSource.onerror = () => {
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, [queryClient]);

  const handleSendRequest = async (email: string) => {
    setErrorMessage('');
    try {
      await sendRequestMutation.mutateAsync(email);
    } catch (error: any) {
      setErrorMessage(error.response?.data?.message || 'Không thể gửi lời mời.');
    }
  };

  const handleRespond = async (requestId: string, accept: boolean) => {
    setErrorMessage('');
    try {
      await respondRequestMutation.mutateAsync({ requestId, accept });
    } catch (error: any) {
      setErrorMessage(error.response?.data?.message || 'Không thể xử lý lời mời.');
    }
  };

  const handleRemoveFriend = async (friendEmail: string) => {
    try {
      await removeFriendMutation.mutateAsync(friendEmail);
    } catch {
      setErrorMessage('Không thể xóa bạn.');
    }
  };

  const openChat = (friendEmail: string) => {
    setChatWith(friendEmail);
  };

  const sendChatMessage = async () => {
    if (!chatWith || !chatInput.trim()) return;
    try {
      await sendMessageMutation.mutateAsync({ to: chatWith, content: chatInput.trim() });
      setChatInput('');
    } catch {
      setErrorMessage('Không thể gửi tin nhắn.');
    }
  };

  const handleSaveProfile = async () => {
    setErrorMessage('');
    try {
      await updateProfileMutation.mutateAsync({
        displayName,
        avatarUrl,
        bio,
      });
      setEditMode(false);
    } catch {
      setErrorMessage('Không thể cập nhật hồ sơ.');
    }
  };

  if (isProfileLoading) {
    return <div className="loading-screen">Đang tải thông tin...</div>;
  }
  
  if (isProfileError) {
    return <div className="loading-screen">Không tải được thông tin người dùng.</div>;
  }

  return (
    <div className="profile-page">
      <div className="profile-header glass-panel">
        <div className="profile-avatar-section">
          <img
            className="profile-avatar"
            src={profile?.avatarUrl || DEFAULT_AVATAR}
            alt="Avatar"
          />
          <div>
            <h2>{profile?.displayName || profile?.email}</h2>
            <p className="text-muted">{profile?.role}</p>
            <p className="text-muted">Tham gia: {new Date(profile?.createdAt || '').toLocaleDateString()}</p>
          </div>
        </div>
      </div>

      <div className="profile-tabs glass-panel">
        <button className={`glass-btn ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>Hồ sơ</button>
        <button className={`glass-btn ${activeTab === 'friends' ? 'active' : ''}`} onClick={() => setActiveTab('friends')}>Bạn bè</button>
        <button className={`glass-btn ${activeTab === 'requests' ? 'active' : ''}`} onClick={() => setActiveTab('requests')}>Lời mời</button>
        <button className={`glass-btn ${activeTab === 'search' ? 'active' : ''}`} onClick={() => setActiveTab('search')}>Tìm bạn</button>
      </div>

      {notification && <div className="notification-banner glass-panel">{notification}</div>}

      <div className="profile-body">
        {activeTab === 'profile' && (
          <section className="glass-panel profile-section profile-detail-section">
            <div className="profile-section-header">
              <h3>Thông tin hồ sơ</h3>
              <button className="glass-btn" onClick={() => setEditMode(!editMode)}>
                {editMode ? 'Hủy' : 'Chỉnh sửa'}
              </button>
            </div>

            {editMode ? (
              <div className="profile-edit-form">
                <label>
                  Tên hiển thị
                  <input className="glass-input" value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
                </label>
                <label>
                  URL avatar
                  <input className="glass-input" value={avatarUrl} onChange={(e) => setAvatarUrl(e.target.value)} />
                </label>
                <label>
                  Bio
                  <textarea className="glass-input" value={bio} onChange={(e) => setBio(e.target.value)} rows={4} />
                </label>
                <button className="primary-btn" onClick={handleSaveProfile}>Lưu thay đổi</button>
              </div>
            ) : (
              <div className="profile-summary">
                <p>{profile?.bio || 'Chưa có mô tả.'}</p>
              </div>
            )}
          </section>
        )}

        {activeTab === 'friends' && (
          <section className="glass-panel profile-section">
            <div className="profile-section-header">
              <h3>Danh sách bạn bè</h3>
            </div>
            {friends.length === 0 ? (
              <p>Bạn chưa có bạn bè nào.</p>
            ) : (
              <ul className="friends-list">
                {friends.map((friend) => (
                  <li key={friend.email} className="friend-item">
                    <div className="user-info">
                      <img className="search-avatar" src={friend.avatarUrl || DEFAULT_AVATAR} alt="avatar" />
                      <div>
                        <div>{friend.displayName || friend.email}</div>
                        <div className="text-muted">{friend.email}</div>
                      </div>
                    </div>
                    <div style={{display: 'flex', gap: '0.5rem', alignItems: 'center'}}>
                      <button className="glass-btn" onClick={() => openChat(friend.email)}>Nhắn tin</button>
                      <button className="glass-btn" onClick={() => handleRemoveFriend(friend.email)}>Xóa bạn</button>
                      <span className="status-pill status-friend">Bạn bè</span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        )}

        {chatWith && (
          <section className="glass-panel profile-section" style={{position: 'fixed', right: 24, bottom: 24, width: 360, maxHeight: '60vh', overflow: 'auto'}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
              <strong>Chat với {chatWith}</strong>
              <button className="glass-btn" onClick={() => setChatWith(null)}>Đóng</button>
            </div>
            <div style={{marginTop: 8, display: 'flex', flexDirection: 'column', gap: 8}}>
              {chatMessages.map((m: any) => (
                <div key={m.id} style={{alignSelf: m.senderEmail === profile?.email ? 'flex-end' : 'flex-start', background: 'rgba(255,255,255,0.04)', padding: 8, borderRadius: 8}}>
                  <div style={{fontSize: 12, color: '#cbd5e1'}}>{m.senderEmail}</div>
                  <div>{m.content}</div>
                  <div style={{fontSize: 11, color: '#94a3b8'}}>{new Date(m.createdAt).toLocaleString()}</div>
                </div>
              ))}
            </div>
            <div style={{display: 'flex', gap: 8, marginTop: 8}}>
              <input value={chatInput} onChange={(e) => setChatInput(e.target.value)} className="glass-input" />
              <button className="primary-btn" onClick={sendChatMessage}>Gửi</button>
            </div>
          </section>
        )}

        {activeTab === 'requests' && (
          <section className="glass-panel profile-section">
            <div className="profile-section-header">
              <h3>Yêu cầu kết bạn</h3>
            </div>
            <div className="request-columns">
              <div>
                <h4>Đến bạn</h4>
                {incoming.length === 0 ? (
                  <p>Không có lời mời.</p>
                ) : (
                  <ul className="request-list">
                    {incoming.map((request) => (
                      <li key={request.id} className="request-item">
                        <div>
                          <div>{request.senderEmail}</div>
                          <div className="text-muted">{request.status}</div>
                        </div>
                        <div>
                          <button className="primary-btn" onClick={() => handleRespond(request.id, true)}>Chấp nhận</button>
                          <button className="glass-btn" onClick={() => handleRespond(request.id, false)}>Từ chối</button>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              <div>
                <h4>Đã gửi</h4>
                {outgoing.length === 0 ? (
                  <p>Chưa gửi lời mời nào.</p>
                ) : (
                  <ul className="request-list">
                    {outgoing.map((request) => (
                      <li key={request.id} className="request-item">
                        <div>
                          <div>{request.receiverEmail}</div>
                          <div className="text-muted">{request.status}</div>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </section>
        )}

        {activeTab === 'search' && (
          <section className="glass-panel profile-section">
            <div className="profile-section-header">
              <h3>Tìm bạn bè</h3>
              <input
                className="glass-input"
                type="search"
                placeholder="Tìm theo email hoặc tên"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </div>
            <ul className="user-list user-search-list">
              {searchResults.length === 0 ? (
                <p>Không tìm thấy người dùng.</p>
              ) : (
                searchResults.map((user) => (
                  <li key={user.email} className="user-item user-search-item">
                    <div className="user-info">
                      <img className="search-avatar" src={user.avatarUrl || DEFAULT_AVATAR} alt="avatar" />
                      <div>
                        <div>{user.displayName || user.email}</div>
                        <div className="text-muted">{user.email}</div>
                      </div>
                    </div>
                    <div className="action-status">
                      <span className={`status-pill status-${user.relationshipStatus.toLowerCase()}`}>
                        {user.relationshipStatus}
                      </span>
                      {user.relationshipStatus === 'NONE' && (
                        <button className="primary-btn" onClick={() => handleSendRequest(user.email)}>
                          Gửi lời mời
                        </button>
                      )}
                    </div>
                  </li>
                ))
              )}
            </ul>
          </section>
        )}
      </div>

      {errorMessage && <div className="error-banner glass-panel">{errorMessage}</div>}
    </div>
  );
}
