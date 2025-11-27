import consts from '../utils/consts.js';
import ensureAccessTokenAuth from './ensureAccessTokenAuth.js';

const ensureDriverAuth = ensureAccessTokenAuth(
  [consts.roles.admin, consts.roles.agencyAdmin, consts.roles.driver],
  'No se cuenta con los privilegios necesarios de conductor o administrador de agencia.',
);
export default ensureDriverAuth;
