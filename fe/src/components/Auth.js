import React, { useState } from 'react';
import Login from './Login';
import Register from './Register';

const Auth = ({ onLogin }) => {
  const [showLogin, setShowLogin] = useState(true);

  const toggleAuthMode = () => {
    setShowLogin(!showLogin);
  };

  return (
    <div>
      {showLogin ? (
        <Login onLogin={onLogin} />
      ) : (
        <Register onRegister={toggleAuthMode} />
      )}
      <button
        onClick={toggleAuthMode}
        className="mt-4 px-3 py-2 rounded-md text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
      >
        {showLogin ? 'Switch to Register' : 'Switch to Login'}
      </button>
    </div>
  );
};

export default Auth;
