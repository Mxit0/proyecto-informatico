// services/userService.js
import {
  getAllUsers,
  getUserById,
  updateUserReputation,
} from '../repositories/userRepository.js';

export async function listUsersService() {
  return await getAllUsers();
}

export async function getUserByIdService(id) {
  return await getUserById(id);
}

export async function updateReputationService(id, reputacion) {
  return await updateUserReputation(id, reputacion);
}
