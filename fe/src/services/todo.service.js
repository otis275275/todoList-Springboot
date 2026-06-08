import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://localhost:8081/api/todos/';

class TodoService {
  getTodos() {
    return axios.get(API_URL, { headers: authHeader() });
  }

  createTodo(title) {
    return axios.post(API_URL, { title }, { headers: authHeader() });
  }

  updateTodo(id, todo) {
    return axios.put(API_URL + id, todo, { headers: authHeader() });
  }

  deleteTodo(id) {
    return axios.delete(API_URL + id, { headers: authHeader() });
  }
}

export default new TodoService();
