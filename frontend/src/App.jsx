import { Routes, Route, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import Layout from "./components/layout/Layout";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import HospitalRegistration from "./pages/auth/HospitalRegistration";
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
import PharmacistList from "./pages/pharmacists/PharmacistList";
import PharmacistForm from "./pages/pharmacists/PharmacistForm";
import LabTechnicianList from "./pages/lab-technicians/LabTechnicianList";
import LabTechnicianForm from "./pages/lab-technicians/LabTechnicianForm";
import ReceptionistList from "./pages/receptionists/ReceptionistList";
import ReceptionistForm from "./pages/receptionists/ReceptionistForm";
import DoctorAssignmentList from "./pages/assignments/DoctorAssignmentList";
import NurseAssignmentList from "./pages/assignments/NurseAssignmentList";
import LabTestList from "./pages/labTests/LabTestList";
import MedicationRequestList from "./pages/medicationRequests/MedicationRequestList";
import MedicationRequestForm from "./pages/medicationRequests/MedicationRequestForm";
import PendingApprovals from "./pages/approvals/PendingApprovals";
import Reports from "./pages/reports/Reports";
import OTRequestList from "./pages/otRequests/OTRequestList";
import OTRequestForm from "./pages/otRequests/OTRequestForm";
import OTRequestDetail from "./pages/otRequests/OTRequestDetail";
import EmergencyDashboard from "./pages/emergency/EmergencyDashboard";
import EmergencyAdmitForm from "./pages/emergency/EmergencyAdmitForm";
import EmergencyRoomDetail from "./pages/emergency/EmergencyRoomDetail";
import EmergencyPatientDetail from "./pages/emergency/EmergencyPatientDetail";
import MedicineOrderList from "./pages/medicineOrders/MedicineOrderList";
import MedicineOrderForm from "./pages/medicineOrders/MedicineOrderForm";
import MedicineOrderDetail from "./pages/medicineOrders/MedicineOrderDetail";
import MedicineOrderPayments from "./pages/medicineOrders/MedicineOrderPayments";

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
        path="/hospital-registration"
        element={
          <PublicRoute>
            <HospitalRegistration />
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
          <Route path="edit/:id" element={<AppointmentForm />} />
          <Route path=":id" element={<AppointmentForm />} />
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

        <Route path="pharmacists">
          <Route index element={<PharmacistList />} />
          <Route path="new" element={<PharmacistForm />} />
          <Route path="edit/:id" element={<PharmacistForm />} />
        </Route>

        <Route path="lab-technicians">
          <Route index element={<LabTechnicianList />} />
          <Route path="new" element={<LabTechnicianForm />} />
          <Route path="edit/:id" element={<LabTechnicianForm />} />
        </Route>

        <Route path="receptionists">
          <Route index element={<ReceptionistList />} />
          <Route path="new" element={<ReceptionistForm />} />
          <Route path="edit/:id" element={<ReceptionistForm />} />
        </Route>

        <Route path="doctor-assignments">
          <Route index element={<DoctorAssignmentList />} />
        </Route>

        <Route path="nurse-assignments">
          <Route index element={<NurseAssignmentList />} />
        </Route>

        <Route path="lab-tests">
          <Route index element={<LabTestList />} />
        </Route>

        <Route path="medication-requests">
          <Route index element={<MedicationRequestList />} />
          <Route path="new" element={<MedicationRequestForm />} />
        </Route>

        <Route path="approvals">
          <Route index element={<PendingApprovals />} />
        </Route>

        <Route path="reports">
          <Route index element={<Reports />} />
        </Route>

        <Route path="ot-requests">
          <Route index element={<OTRequestList />} />
          <Route path="new" element={<OTRequestForm />} />
          <Route path=":id" element={<OTRequestDetail />} />
        </Route>

        <Route path="emergency">
          <Route index element={<EmergencyDashboard />} />
          <Route path="admit" element={<EmergencyAdmitForm />} />
          <Route path="rooms/:id" element={<EmergencyRoomDetail />} />
          <Route path="patients/:id" element={<EmergencyPatientDetail />} />
        </Route>

        <Route path="medicine-orders">
          <Route index element={<MedicineOrderList />} />
          <Route path="new" element={<MedicineOrderForm />} />
          <Route path="payments" element={<MedicineOrderPayments />} />
          <Route path=":id" element={<MedicineOrderDetail />} />
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
