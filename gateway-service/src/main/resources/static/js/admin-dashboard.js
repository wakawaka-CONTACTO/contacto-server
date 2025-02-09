let currentPage = 0;
const pageSize = 10;
const token = localStorage.getItem('accessToken');

// 페이지 로드 시 유저 목록 가져오기
window.onload = () => {
    console.log('Checking authentication...'); // 인증 확인
    if (!token) {
        console.log('No token found, redirecting to login'); // 토큰 없음
        window.location.href = 'admin.html';
        return;
    }
    loadUsers(currentPage);
};

// 유저 목록 로드
async function loadUsers(page) {
    try {
        document.getElementById('loadingSpinner').style.display = 'flex';
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/v1/users/admin/users?page=${page}&size=${pageSize}`, {
            headers: {
                'Authorization': token
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                console.log('Token expired or invalid, redirecting to login'); // 401 에러
                window.location.href = 'admin.html';
                return;
            }
            throw new Error('Failed to load users');
        }

        const data = await response.json();
        console.log('Users loaded:', data); // 응답 데이터 확인
        if (Array.isArray(data)) {
            displayUsers(data);
        } else {
            console.error('Expected an array but got:', data);
        }
        updatePagination(data.totalPages);
    } catch (error) {
        console.error('Error loading users:', error);
    } finally {
        document.getElementById('loadingSpinner').style.display = 'none';
    }
}

// 유저 목록 표시
function displayUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';

    users.forEach(item => {
        const user = item.userDto;
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.name || '이름 없음'}</td>
            <td>
                ${user.portfolioImageUrl ? 
                    `<img src="${user.portfolioImageUrl}" alt="프로필" style="width: 50px; height: 50px; object-fit: cover; border-radius: 25px;">` : 
                    '이미지 없음'
                }
            </td>
            <td>${item.isConnected ? '연결됨' : '연결 안됨'}</td>
            <td>
                <button onclick="location.href='user-detail.html?userId=${user.id}'">상세보기</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// 페이지네이션 업데이트
function updatePagination(totalPages) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    for (let i = 0; i < totalPages; i++) {
        const button = document.createElement('button');
        button.textContent = i + 1;
        button.onclick = () => {
            currentPage = i;
            loadUsers(currentPage);
        };
        if (i === currentPage) {
            button.classList.add('active');
        }
        pagination.appendChild(button);
    }
}

// 유저 검색
async function searchUser() {
    try {
        const searchTerm = document.getElementById('searchInput').value;
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/v1/users/admin/users/search?name=${searchTerm}`, {
            headers: {
                'Authorization': token
            }
        });

        if (!response.ok) {
            throw new Error('Failed to search users');
        }

        const users = await response.json();
        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = '';

        users.forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.userDto.id}</td>
                <td>${item.userDto.name}</td>
                <td>
                    <img src="${item.userDto.portfolioImageUrl}" alt="프로필 이미지" 
                        style="width: 50px; height: 50px; border-radius: 25px; object-fit: cover;">
                </td>
                <td>
                    <span class="status-badge ${item.isConnected ? 'status-connected' : 'status-disconnected'}">
                        ${item.isConnected ? '접속중' : '미접속'}
                    </span>
                </td>
                <td>
                    <button onclick="location.href='user-detail.html?userId=${item.userDto.id}'">상세보기</button>
                </td>
            `;
            tbody.appendChild(row);
        });

    } catch (error) {
        console.error('Error searching user:', error);
    }
}

// 검색창 엔터 키 이벤트
document.getElementById('searchInput').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        searchUser();
    }
});