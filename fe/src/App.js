import React, { useState, useEffect } from 'react';
import AuthService from './services/auth.service';
import TodoService from './services/todo.service';
import Auth from './components/Auth';
import TodoList from './components/TodoList';
import Header from './components/Header';
import BottomNav from './components/BottomNav';
import SideNav from './components/SideNav';

function App() {
  const [currentUser, setCurrentUser] = useState(undefined);
  const [todos, setTodos] = useState([]);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
      loadTodos();
    }
    // Add script for header shadow
    window.addEventListener('scroll', () => {
        const header = document.getElementById('main-header');
        if (window.scrollY > 10) {
            header.classList.add('shadow-sm');
        } else {
            header.classList.remove('shadow-sm');
        }
    });
  }, []);

  const loadTodos = async () => {
    try {
      const response = await TodoService.getTodos();
      setTodos(response.data);
    } catch (error) {
      console.log(error);
    }
  };

  const handleLogin = () => {
    const user = AuthService.getCurrentUser();
    setCurrentUser(user);
    loadTodos();
  };

  const handleLogout = () => {
    AuthService.logout();
    setCurrentUser(undefined);
    setTodos([]);
  };

  return (
    <div className="bg-background text-on-background min-h-screen pb-24 font-body-md selection:bg-primary-container selection:text-on-primary-container">
      <Header />
      <SideNav />
      <main className="w-full max-w-[768px] mx-auto px-margin-mobile pt-md">
        {currentUser ? (
          <div>
            <section className="mb-xl">
              <h2 className="font-headline-lg-mobile text-headline-lg-mobile md:font-display-lg md:text-display-lg text-on-surface">
                Good morning, {currentUser.username}.
              </h2>
              <p className="font-body-lg text-body-lg text-on-surface-variant mt-sm">
                You have {todos.filter(t => !t.completed).length} high-priority tasks today.
              </p>
            </section>
            <TodoList todos={todos} loadTodos={loadTodos} />
            <button
                onClick={handleLogout}
                className="mt-4 px-4 py-2 bg-red-500 text-white rounded"
            >
                Logout
            </button>
          </div>
        ) : (
          <Auth onLogin={handleLogin} />
        )}
      </main>
      <BottomNav />
    </div>
  );
}

export default App;
