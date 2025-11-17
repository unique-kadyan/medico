import { Routes, Route, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import Layout from "./components/layout/Layout";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Dashboard from "./pages/dashboard/Dashboard";
import PatientList from "./pages/patients/PatientList";
import PatientDetail from "./pages/patients/PatientDetail";
import PatientForm from "./pages/patients/PatientForm";
import MedicationList from "./pages/medications/MedicationList";
import MedicationForm from "./pages/medications/MedicationForm";
import AppointmentList from "./pages/appointments/AppointmentList";
import AppointmentForm from "./pages/appointments/AppointmentForm";
import DoctorList from "./pages/doctors/DoctorList";
import DoctorDetail from "./pages/doctors/DoctorDetail";
import DoctorForm from "./pages/doctors/DoctorForm";
import NurseList from "./pages/nurses/NurseList";
import NurseForm from "./pages/nurses/NurseForm";
import AssignmentList from "./pages/assignments/AssignmentList";
import LabTestList from "./pages/labTests/LabTestList";
import MedicationRequestList from "./pages/medicationRequests/MedicationRequestList";
import PendingApprovals from "./pages/approvals/PendingApprovals";

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useSelector((state) => state.auth);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function PublicRoute({ children }) {
  const { isAuthenticated } = useSelector((state) => state.auth);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicRoute>
            <Register />
          </PublicRoute>
        }
      />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />

        <Route path="patients">
          <Route index element={<PatientList />} />
          <Route path="new" element={<PatientForm />} />
          <Route path=":id" element={<PatientDetail />} />
          <Route path=":id/edit" element={<PatientForm />} />
        </Route>

        <Route path="medications">
          <Route index element={<MedicationList />} />
          <Route path="new" element={<MedicationForm />} />
          <Route path=":id/edit" element={<MedicationForm />} />
        </Route>

        <Route path="appointments">
          <Route index element={<AppointmentList />} />
          <Route path="new" element={<AppointmentForm />} />
          <Route path=":id/edit" element={<AppointmentForm />} />
        </Route>

        <Route path="doctors">
          <Route index element={<DoctorList />} />
          <Route path="new" element={<DoctorForm />} />
          <Route path=":id" element={<DoctorDetail />} />
          <Route path="edit/:id" element={<DoctorForm />} />
        </Route>

        <Route path="nurses">
          <Route index element={<NurseList />} />
          <Route path="new" element={<NurseForm />} />
          <Route path="edit/:id" element={<NurseForm />} />
        </Route>

        <Route path="assignments">
          <Route index element={<AssignmentList />} />
        </Route>

        <Route path="lab-tests">
          <Route index element={<LabTestList />} />
        </Route>

        <Route path="medication-requests">
          <Route index element={<MedicationRequestList />} />
        </Route>

        <Route path="approvals">
          <Route index element={<PendingApprovals />} />
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
